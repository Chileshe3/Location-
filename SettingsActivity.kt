package com.example.chat22.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.chat22.MainActivity
import com.example.chat22.composables.SettingsScreen
import com.example.chat22.models.User
import com.example.chat22.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    private var currentUser by mutableStateOf<User?>(null)
    private var userListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is authenticated
        if (FirebaseUtils.getCurrentUser() == null) {
            finish()
            return
        }

        setContent {
            SettingsScreen(
                currentUser = currentUser,
                onBackClick = { finish() },
                onEditProfile = {
                    Toast.makeText(this@SettingsActivity, "Edit Profile coming soon", Toast.LENGTH_SHORT).show()
                },
                onPrivacyAndSecurity = {
                    Toast.makeText(this@SettingsActivity, "Privacy & Security coming soon", Toast.LENGTH_SHORT).show()
                },
                onNotifications = {
                    Toast.makeText(this@SettingsActivity, "Notifications settings coming soon", Toast.LENGTH_SHORT).show()
                },
                onChatSettings = {
                    Toast.makeText(this@SettingsActivity, "Chat settings coming soon", Toast.LENGTH_SHORT).show()
                },
                onAppearance = {
                    Toast.makeText(this@SettingsActivity, "Appearance settings coming soon", Toast.LENGTH_SHORT).show()
                },
                onStorageAndData = {
                    Toast.makeText(this@SettingsActivity, "Storage & Data coming soon", Toast.LENGTH_SHORT).show()
                },
                onLanguage = {
                    Toast.makeText(this@SettingsActivity, "Language settings coming soon", Toast.LENGTH_SHORT).show()
                },
                onHelp = {
                    Toast.makeText(this@SettingsActivity, "Help & Support coming soon", Toast.LENGTH_SHORT).show()
                },
                onInviteFriends = {
                    shareApp()
                },
                onBackupRestore = {
                    Toast.makeText(this@SettingsActivity, "Backup & Restore coming soon", Toast.LENGTH_SHORT).show()
                },
                onAdvanced = {
                    Toast.makeText(this@SettingsActivity, "Advanced settings coming soon", Toast.LENGTH_SHORT).show()
                },
                onAbout = {
                    showAboutDialog()
                },
                onDeleteAccount = {
                    handleDeleteAccount()
                },
                onLogout = {
                    handleLogout()
                }
            )
        }

        setupUserListener()
    }

    private fun setupUserListener() {
        val currentUserId = FirebaseUtils.getCurrentUserId()
        if (currentUserId.isEmpty()) {
            finish()
            return
        }

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        currentUser = user
                        Log.d("SettingsActivity", "User data loaded: ${user.username}")
                    } else {
                        Log.w("SettingsActivity", "User data is null")
                    }
                } catch (e: Exception) {
                    Log.e("SettingsActivity", "Error loading user data: ${e.message}", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SettingsActivity", "User listener cancelled: ${error.message}")
            }
        }

        FirebaseUtils.usersRef.child(currentUserId).addValueEventListener(userListener!!)
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, 
                    "Hey! Check out this awesome messaging app: [App Link Coming Soon]")
                putExtra(Intent.EXTRA_SUBJECT, "Join me on @Besa Chat!")
            }
            startActivity(Intent.createChooser(shareIntent, "Invite Friends"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing app", Toast.LENGTH_SHORT).show()
            Log.e("SettingsActivity", "Error sharing app: ${e.message}", e)
        }
    }

    private fun showAboutDialog() {
        Toast.makeText(this, 
            "@Besa Chat v1.0\nSecure messaging for everyone\nBuilt with ❤️", 
            Toast.LENGTH_LONG).show()
    }

    private fun handleDeleteAccount() {
        lifecycleScope.launch {
            try {
                // TODO: Implement account deletion logic
                // 1. Delete user data from Firebase
                // 2. Delete user messages
                // 3. Clean up storage
                // 4. Delete Firebase Auth account
                
                Toast.makeText(this@SettingsActivity, 
                    "Account deletion will be implemented soon", 
                    Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, 
                    "Error deleting account: ${e.message}", 
                    Toast.LENGTH_LONG).show()
                Log.e("SettingsActivity", "Error deleting account: ${e.message}", e)
            }
        }
    }

    private fun handleLogout() {
        try {
            // Update user offline status
            FirebaseUtils.updateUserOnlineStatus(false)
            
            // Remove listeners
            detachUserListener()
            
            // Sign out from Firebase
            FirebaseUtils.auth.signOut()
            
            // Navigate back to MainActivity (which will redirect to login)
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error during logout: ${e.message}", e)
            // Force logout even if there's an error
            FirebaseUtils.auth.signOut()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun detachUserListener() {
        userListener?.let { listener ->
            val currentUserId = FirebaseUtils.getCurrentUserId()
            if (currentUserId.isNotEmpty()) {
                FirebaseUtils.usersRef.child(currentUserId).removeEventListener(listener)
            }
        }
        userListener = null
    }

    override fun onResume() {
        super.onResume()
        FirebaseUtils.updateUserOnlineStatus(true)
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtils.updateUserOnlineStatus(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        detachUserListener()
    }
}
