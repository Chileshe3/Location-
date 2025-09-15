@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalTopBar(
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    onNewGroupClick: () -> Unit,
    onNewBroadcastClick: () -> Unit,
    onLinkedDevicesClick: () -> Unit,
    onStarredMessagesClick: () -> Unit,
    onPaymentsClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "@Besa",
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp
            )
        },
        actions = {
            // Camera Icon
            IconButton(onClick = { /* TODO: Camera */ }) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera"
                )
            }

            // Search Icon
            IconButton(onClick = { /* TODO: Global search */ }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            }

            // More Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("New group") },
                        onClick = {
                            showMenu = false
                            onNewGroupClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Group, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("New broadcast") },
                        onClick = {
                            showMenu = false
                            onNewBroadcastClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Campaign, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Linked devices") },
                        onClick = {
                            showMenu = false
                            onLinkedDevicesClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Devices, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Starred messages") },
                        onClick = {
                            showMenu = false
                            onStarredMessagesClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Star, contentDescription = null)
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Payments") },
                        onClick = {
                            showMenu = false
                            onPaymentsClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Payment, contentDescription = null)
                        }
                    )
                    
                    Divider()
                    
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            showMenu = false
                            onSettingsClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Sign out") },
                        onClick = {
                            showMenu = false
                            onLogout()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ExitToApp, contentDescription = null)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
