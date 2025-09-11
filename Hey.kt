package com.example.chat22.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.chat22.composables.SignalChatScreen
import com.example.chat22.models.Message
import com.example.chat22.models.User
import com.example.chat22.utils.FirebaseUtils
import com.example.chat22.utils.LocalMessageStorage
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.Query

class ChatActivity : ComponentActivity() {
    private val messages = mutableStateListOf<Message>()
    private var receiverUser by mutableStateOf<User?>(null)
    private var messageText by mutableStateOf("")
    private lateinit var localStorage: LocalMessageStorage
    private lateinit var receiverId: String
    private val processedMessageIds = mutableSetOf<String>()

    // Store references to listeners for proper cleanup
    private var messageListener: ChildEventListener? = null
    private var messageQuery: Query? = null
    private var receiverUserListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        receiverId = intent.getStringExtra("userId") ?: run {
            finish()
            return
        }

        // Initialize local storage
        localStorage = LocalMessageStorage(this)

        // Load local messages first for instant display
        loadLocalMessages()

        // Load receiver user info
        loadReceiverUser(receiverId)

        // Setup Firebase listener for new messages
        setupMessageListener(receiverId)

        // Try to resend any failed messages
        FirebaseUtils.resendFailedMessages(this)

        // IMPORTANT: Mark all messages from this user as read when opening chat
        markAllMessagesAsRead()

        setContent {
            SignalChatScreen(
                messages = messages,
                receiverUser = receiverUser,
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    sendMessage(messageText.trim(), receiverId)
                    messageText = ""
                },
                onBackPressed = { finish() },
                getCurrentUserId = { FirebaseUtils.getCurrentUserId() }
            )
        }
    }

    private fun markAllMessagesAsRead() {
        val currentUserId = FirebaseUtils.getCurrentUserId()
        val updatedCount = localStorage.markMessagesAsRead(currentUserId, receiverId)
        if (updatedCount > 0) {
            Log.d("ChatActivity", "Marked $updatedCount messages as read for user: $receiverId")
            
            // Also update the messages in the UI list
            for (i in messages.indices) {
                if (messages[i].senderId == receiverId && !messages[i].isRead) {
                    messages[i] = messages[i].copy(isRead = true)
                }
            }
        }
    }

    private fun loadReceiverUser(userId: String) {
        receiverUserListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                receiverUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Failed to load receiver user: ${error.message}")
            }
        }

        FirebaseUtils.usersRef.child(userId).addListenerForSingleValueEvent(receiverUserListener!!)
    }

    private fun loadLocalMessages() {
        val currentUserId = FirebaseUtils.getCurrentUserId()
        val localMessages = localStorage.getMessages(currentUserId, receiverId)

        Log.d("ChatActivity", "Loading ${localMessages.size} local messages")

        messages.clear()
        messages.addAll(localMessages.sortedBy { it.timestamp })

        // Keep track of processed messages to avoid duplicates
        localMessages.forEach { message ->
            if (message.firebaseId.isNotEmpty()) {
                processedMessageIds.add(message.firebaseId)
            }
        }
    }

    private fun setupMessageListener(receiverId: String) {
        val currentUserId = FirebaseUtils.getCurrentUserId()

        // Create optimized query - only listen to messages from the last 24 hours
        // and limit to recent messages to reduce data transfer
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        messageQuery = FirebaseUtils.messagesRef
            .orderByChild("timestamp")
            .startAt(oneDayAgo.toDouble())
            .limitToLast(100) // Limit to last 100 messages

        messageListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                val messageId = snapshot.key ?: return

                message?.let { msg ->
                    // Check if this message is part of our conversation
                    if ((msg.senderId == currentUserId && msg.receiverId == receiverId) ||
                        (msg.senderId == receiverId && msg.receiverId == currentUserId)) {

                        // Avoid duplicates from local storage
                        if (!processedMessageIds.contains(messageId)) {
                            // Add Firebase ID to the message for local storage
                            // IMPORTANT: Mark incoming messages as read since user is viewing the chat
                            val isFromReceiver = msg.senderId == receiverId
                            val messageWithId = msg.copy(
                                firebaseId = messageId,
                                isRead = if (isFromReceiver) true else msg.isRead // Mark as read if it's from the receiver
                            )

                            // Save to local storage
                            localStorage.saveMessage(messageWithId)

                            // Add to UI only if not already present
                            val existingIndex = messages.indexOfFirst {
                                it.timestamp == msg.timestamp && it.senderId == msg.senderId && it.messageText == msg.messageText
                            }

                            if (existingIndex == -1) {
                                // Insert in correct position based on timestamp
                                val insertIndex = messages.binarySearch { it.timestamp.compareTo(msg.timestamp) }.let { index ->
                                    if (index < 0) -index - 1 else index
                                }

                                messages.add(insertIndex, messageWithId)

                                Log.d("ChatActivity", "New message added from Firebase: ${msg.messageText}")
                            }

                            processedMessageIds.add(messageId)
                        }
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("ChatActivity", "Message changed: ${snapshot.key}")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("ChatActivity", "Message removed: ${snapshot.key}")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("ChatActivity", "Message moved: ${snapshot.key}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatActivity", "Firebase listener cancelled: ${error.message}")
            }
        }

        messageQuery?.addChildEventListener(messageListener!!)
    }

    private fun sendMessage(messageText: String, receiverId: String) {
        if (messageText.isEmpty()) return

        val currentUserId = FirebaseUtils.getCurrentUserId()

        val message = Message(
            senderId = currentUserId,
            receiverId = receiverId,
            messageText = messageText,
            timestamp = System.currentTimeMillis()
        )

        // Add message to local list immediately for instant UI feedback
        messages.add(message)

        Log.d("ChatActivity", "Sending message: $messageText")

        // Send with local storage and automatic retry
        FirebaseUtils.sendMessageWithLocalStorage(message, this) { success, messageId ->
            runOnUiThread {
                if (success) {
                    Log.d("ChatActivity", "Message sent successfully: $messageId")
                    // Update the message in local list with Firebase ID
                    val messageIndex = messages.indexOfFirst {
                        it.localId == message.localId
                    }
                    if (messageIndex != -1) {
                        messages[messageIndex] = message.copy(firebaseId = messageId)
                        processedMessageIds.add(messageId)
                    }
                } else {
                    Log.w("ChatActivity", "Message failed to send, will retry automatically")
                    // The message is already saved locally and will be retried automatically
                }
            }
        }
    }

    private fun attachListeners() {
        // Re-attach listeners when coming back to foreground
        if (messageListener != null && messageQuery != null) {
            messageQuery?.addChildEventListener(messageListener!!)
        }
    }

    private fun detachListeners() {
        // Remove Firebase listeners to prevent background processing
        messageListener?.let { listener ->
            messageQuery?.removeEventListener(listener)
        }

        receiverUserListener?.let { listener ->
            FirebaseUtils.usersRef.child(receiverId).removeEventListener(listener)
        }
    }

    override fun onResume() {
        super.onResume()
        // Try to resend any failed messages when user returns to chat
        FirebaseUtils.resendFailedMessages(this)

        // Update online status
        FirebaseUtils.updateUserOnlineStatus(true)

        // Re-attach listeners if they were detached
        attachListeners()

        // IMPORTANT: Mark messages as read when user returns to the chat
        markAllMessagesAsRead()
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtils.updateUserOnlineStatus(false)
    }

    // CRITICAL: Detach listeners when activity goes to background
    override fun onStop() {
        super.onStop()
        detachListeners()
        Log.d("ChatActivity", "Firebase listeners detached - saving battery")
    }

    // CRITICAL: Cleanup when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        detachListeners()
        Log.d("ChatActivity", "ChatActivity destroyed, all listeners removed")
    }
}
