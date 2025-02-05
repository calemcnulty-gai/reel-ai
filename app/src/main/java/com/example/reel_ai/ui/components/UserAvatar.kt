package com.example.reel_ai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatar(
    onNavigateToProfile: () -> Unit,
    onNavigateToUpload: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { showMenu = true }
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Menu",
                modifier = Modifier.size(32.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Upload Video") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = null
                    )
                },
                onClick = {
                    showMenu = false
                    onNavigateToUpload()
                }
            )
            
            DropdownMenuItem(
                text = { Text("Profile") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },
                onClick = {
                    showMenu = false
                    onNavigateToProfile()
                }
            )
            
            Divider()
            
            DropdownMenuItem(
                text = { Text("Sign Out") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null
                    )
                },
                onClick = {
                    showMenu = false
                    onSignOut()
                }
            )
        }
    }
} 