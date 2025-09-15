package com.example.chat22.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chat22.models.User

// Signal's color scheme
private val SignalBlue = Color(0xFF2090EA)
private val SignalDarkGray = Color(0xFF1B1B1D)
private val SignalRed = Color(0xFFDC3545)
private val SignalGreen = Color(0xFF00D924)

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

data class SettingsItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconTint: Color = Color.Unspecified,
    val trailing: (@Composable () -> Unit)? = null,
    val onClick: () -> Unit = {},
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User?,
    onBackClick: () -> Unit,
    onEditProfile: () -> Unit = {},
    onPrivacyAndSecurity: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onChatSettings: () -> Unit = {},
    onAppearance: () -> Unit = {},
    onStorageAndData: () -> Unit = {},
    onLanguage: () -> Unit = {},
    onHelp: () -> Unit = {},
    onInviteFriends: () -> Unit = {},
    onBackupRestore: () -> Unit = {},
    onAdvanced: () -> Unit = {},
    onAbout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Profile Section
            item {
                ProfileSection(
                    user = currentUser,
                    onEditProfile = onEditProfile
                )
            }

            // Quick Actions
            item {
                QuickActionsSection(
                    onInviteFriends = onInviteFriends,
                    onBackupRestore = onBackupRestore
                )
            }

            // Settings Sections
            val settingsSections = listOf(
                SettingsSection(
                    title = "Account",
                    items = listOf(
                        SettingsItem(
                            title = "Privacy and Security",
                            subtitle = "Block contacts, disappearing messages",
                            icon = Icons.Outlined.Security,
                            onClick = onPrivacyAndSecurity
                        ),
                        SettingsItem(
                            title = "Account",
                            subtitle = "Change number, delete account",
                            icon = Icons.Outlined.AccountCircle,
                            onClick = { /* TODO: Account settings */ }
                        )
                    )
                ),
                SettingsSection(
                    title = "Messaging",
                    items = listOf(
                        SettingsItem(
                            title = "Notifications",
                            subtitle = "Message, group, and call tones",
                            icon = Icons.Outlined.Notifications,
                            onClick = onNotifications
                        ),
                        SettingsItem(
                            title = "Chats",
                            subtitle = "Theme, wallpapers, chat history",
                            icon = Icons.Outlined.Chat,
                            onClick = onChatSettings
                        ),
                        SettingsItem(
                            title = "Calls",
                            subtitle = "Calling options and settings",
                            icon = Icons.Outlined.Call,
                            onClick = { /* TODO: Call settings */ }
                        ),
                        SettingsItem(
                            title = "Storage and Data",
                            subtitle = "Network usage, auto-download",
                            icon = Icons.Outlined.Storage,
                            onClick = onStorageAndData
                        )
                    )
                ),
                SettingsSection(
                    title = "Personalization",
                    items = listOf(
                        SettingsItem(
                            title = "Appearance",
                            subtitle = "Dark mode, chat colors",
                            icon = Icons.Outlined.Palette,
                            onClick = onAppearance
                        ),
                        SettingsItem(
                            title = "Language",
                            subtitle = "English",
                            icon = Icons.Outlined.Language,
                            onClick = onLanguage
                        ),
                        SettingsItem(
                            title = "Accessibility",
                            subtitle = "Font size, screen reader",
                            icon = Icons.Outlined.Accessibility,
                            onClick = { /* TODO: Accessibility */ }
                        )
                    )
                ),
                SettingsSection(
                    title = "Advanced",
                    items = listOf(
                        SettingsItem(
                            title = "Advanced",
                            subtitle = "Proxy, linked devices",
                            icon = Icons.Outlined.Settings,
                            onClick = onAdvanced
                        ),
                        SettingsItem(
                            title = "Backup and Restore",
                            subtitle = "Chat backup to cloud",
                            icon = Icons.Outlined.CloudUpload,
                            onClick = onBackupRestore
                        )
                    )
                ),
                SettingsSection(
                    title = "Support",
                    items = listOf(
                        SettingsItem(
                            title = "Help",
                            subtitle = "FAQ, contact us, privacy policy",
                            icon = Icons.Outlined.Help,
                            onClick = onHelp
                        ),
                        SettingsItem(
                            title = "About",
                            subtitle = "Version, terms of service",
                            icon = Icons.Outlined.Info,
                            onClick = onAbout
                        )
                    )
                ),
                SettingsSection(
                    title = "Account Actions",
                    items = listOf(
                        SettingsItem(
                            title = "Sign Out",
                            subtitle = "Sign out of your account",
                            icon = Icons.Outlined.ExitToApp,
                            iconTint = SignalRed,
                            onClick = { showLogoutDialog = true },
                            isDestructive = true
                        ),
                        SettingsItem(
                            title = "Delete Account",
                            subtitle = "Permanently delete your account",
                            icon = Icons.Outlined.Delete,
                            iconTint = SignalRed,
                            onClick = { showDeleteAccountDialog = true },
                            isDestructive = true
                        )
                    )
                )
            )

            settingsSections.forEach { section ->
                item {
                    SettingsSectionHeader(section.title)
                }

                items(section.items) { settingsItem ->
                    SettingsItemRow(settingsItem)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = SignalRed)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account") },
            text = { 
                Text("This will permanently delete your account and all your messages. This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = SignalRed)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileSection(
    user: User?,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onEditProfile() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SignalBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.username?.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // User Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user?.username ?: "Unknown User",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = user?.email ?: "No email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Online status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (user?.online == true) SignalGreen else MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = if (user?.online == true) "Online" else "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            // Edit Icon
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit Profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun QuickActionsSection(
    onInviteFriends: () -> Unit,
    onBackupRestore: () -> Unit
) {
    val quickActions = listOf(
        Triple("Invite Friends", Icons.Outlined.PersonAdd, onInviteFriends),
        Triple("Backup", Icons.Outlined.CloudUpload, onBackupRestore),
        Triple("QR Code", Icons.Outlined.QrCode) { /* TODO */ },
        Triple("Starred", Icons.Outlined.Star) { /* TODO */ }
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(quickActions) { (title, icon, onClick) ->
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .clickable { onClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = SignalBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = SignalBlue,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItemRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            modifier = Modifier.size(24.dp),
            tint = when {
                item.iconTint != Color.Unspecified -> item.iconTint
                item.isDestructive -> SignalRed
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.isDestructive) SignalRed else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )

            item.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Trailing content
        item.trailing?.invoke() ?: run {
            if (!item.isDestructive) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
                
