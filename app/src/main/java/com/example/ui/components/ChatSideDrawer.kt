package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatConversation
import com.example.data.SelahPreferences
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import com.example.ui.theme.NightBase
import com.example.ui.theme.NightSurface
import com.example.ui.theme.NightText
import com.example.ui.theme.NightTextSoft

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatSideDrawerContent(
    prefs: SelahPreferences,
    activeConversationId: String,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenProfile: () -> Unit,
    onDeleteConversation: (String) -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (prefs.themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    val primaryTextColor = if (isDark) NightText else Ink
    val secondaryTextColor = if (isDark) NightTextSoft else InkSoft
    val surfaceColor = if (isDark) NightSurface else Color(0xFFFFF9F5)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val activeItemColor = if (isDark) Ember.copy(alpha = 0.2f) else Dawn100

    var conversationToDelete by remember { mutableStateOf<ChatConversation?>(null) }
    val conversations = remember(prefs.getConversations()) { prefs.getConversations() }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(surfaceColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top: Selah Orb + "Selah" wordmark + "New chat" row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                SelahOrb(size = 32.dp, isPulsing = false)
                Text(
                    text = "Selah",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = Ember,
                        fontSize = 24.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "New chat" row
            Surface(
                onClick = onNewChat,
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) NightBase.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_new_chat_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New chat",
                        tint = Ember,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "New chat",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = primaryTextColor,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = dividerColor, thickness = 1.dp)

        // Middle: "Recents" list (newest first)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Recents",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = secondaryTextColor,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
            )

            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent chats yet",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = secondaryTextColor,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        val isSelected = conv.id == activeConversationId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) activeItemColor else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onSelectConversation(conv.id) },
                                    onLongClick = { conversationToDelete = conv }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = if (isSelected) Ember else secondaryTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = conv.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) (if (isDark) Color(0xFFFFB74D) else EmberDeep) else primaryTextColor,
                                            fontSize = 14.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = dividerColor, thickness = 1.dp)

        // Bottom (pinned): Profile row
        val userName = if (prefs.userName.isNotBlank()) prefs.userName else "Friend"
        val firstLetter = userName.first().uppercaseChar().toString()
        val guideName = prefs.guideStyle.replaceFirstChar { it.uppercase() }

        Surface(
            onClick = onOpenProfile,
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drawer_profile_row")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Circle Avatar with first letter
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Ember),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstLetter,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$guideName Guide",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Long press delete confirmation dialog
    conversationToDelete?.let { conv ->
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = { Text("Delete Chat?", color = primaryTextColor) },
            text = { Text("Are you sure you want to delete \"${conv.title}\"?", color = secondaryTextColor) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteConversation(conv.id)
                        conversationToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Ember)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToDelete = null }) {
                    Text("Cancel", color = secondaryTextColor)
                }
            }
        )
    }
}
