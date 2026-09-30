package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatConversation
import com.example.data.SelahPreferences
import com.example.data.VerseRepository
import com.example.ui.components.DawnBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedProfileScreen(
    prefs: SelahPreferences,
    onBack: () -> Unit,
    onOpenVerseDetail: (Int) -> Unit,
    onResumeChat: (conversationId: String) -> Unit,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Verses, 1: Chats, 2: Profile Settings

    var savedVerseIds by remember { mutableStateOf(prefs.getSavedVerseIds()) }
    var conversations by remember { mutableStateOf(prefs.getConversations()) }

    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(prefs.userName) }
    var readAloudSetting by remember { mutableStateOf(prefs.readAloud) }
    var currentThemeMode by remember { mutableStateOf(prefs.themeMode) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    DawnBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.65f))
                        .testTag("saved_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink
                    )
                }

                Text(
                    text = "Saved & Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                )

                Box(modifier = Modifier.size(44.dp))
            }

            // Segmented Tab Controls: Verses | Chats | Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton(
                    label = "Verses (${savedVerseIds.size})",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    testTag = "tab_verses"
                )
                TabButton(
                    label = "Chats (${conversations.size})",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    testTag = "tab_chats"
                )
                TabButton(
                    label = "Profile",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    testTag = "tab_profile"
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Saved Verses Tab
                    if (savedVerseIds.isEmpty()) {
                        EmptyStateView(
                            title = "No saved verses yet",
                            subtitle = "Explore the Library or Verse of the Day and tap the bookmark to save verses."
                        )
                    } else {
                        val versesList = remember(savedVerseIds) {
                            savedVerseIds.mapNotNull { VerseRepository.getById(it) }
                        }
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(versesList, key = { it.id }) { v ->
                                GlassCard(
                                    onClick = { onOpenVerseDetail(v.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = v.reference,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Ink
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "\"${v.text}\"",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = InkSoft,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                prefs.toggleSavedVerse(v.id)
                                                savedVerseIds = prefs.getSavedVerseIds()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Remove",
                                                tint = InkSoft
                                            )
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
                1 -> {
                    // Saved Chats Tab
                    if (conversations.isEmpty()) {
                        EmptyStateView(
                            title = "No conversation history",
                            subtitle = "Start a chat reflection with Selah and your conversations will appear here."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(conversations, key = { it.id }) { conv ->
                                GlassCard(
                                    onClick = { onResumeChat(conv.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = conv.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Ink
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Guide: ${conv.guideStyle.replaceFirstChar { it.uppercase() }} · ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(conv.lastUpdated))}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = InkSoft)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                prefs.deleteConversation(conv.id)
                                                conversations = prefs.getConversations()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = InkSoft
                                            )
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
                2 -> {
                    // Profile & Settings Tab
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // User Name Card
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Your Name",
                                            style = MaterialTheme.typography.labelSmall.copy(color = InkSoft)
                                        )
                                        Text(
                                            text = if (prefs.userName.isBlank()) "Friend" else prefs.userName,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Ink
                                            )
                                        )
                                    }

                                    IconButton(onClick = { isEditingName = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = Ember)
                                    }
                                }
                            }
                        }

                        // Guide Style Selection
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Guide Persona",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val styles = listOf(
                                        Pair("shepherd", "Shepherd (Gentle comfort & prayer)"),
                                        Pair("scholar", "Scholar (History & original context)"),
                                        Pair("storyteller", "Storyteller (Biblical narrative)"),
                                        Pair("friend", "Friend (Everyday life & encouragement)")
                                    )

                                    styles.forEach { (id, label) ->
                                        val isCurrent = prefs.guideStyle == id
                                        Surface(
                                            onClick = {
                                                prefs.guideStyle = id
                                                Toast.makeText(context, "Guide set to $label", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isCurrent) Dawn100 else Color.Transparent,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isCurrent) Ember else Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCurrent) EmberDeep else Ink
                                                ),
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Theme Mode Selection
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Theme Appearance",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("light" to "Dawn Glass", "dark" to "Night Vigil", "system" to "System").forEach { (mode, name) ->
                                            val isSel = currentThemeMode == mode
                                            Surface(
                                                onClick = {
                                                    currentThemeMode = mode
                                                    prefs.themeMode = mode
                                                    onThemeChanged(mode)
                                                },
                                                shape = RoundedCornerShape(999.dp),
                                                color = if (isSel) Ink else Color.White.copy(alpha = 0.6f),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = name,
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSel) Color.White else Ink
                                                    ),
                                                    modifier = Modifier.padding(vertical = 10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Read Replies Aloud Toggle
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Read replies aloud",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Ink
                                            )
                                        )
                                        Text(
                                            text = "Automatically speak Selah's reflections aloud",
                                            style = MaterialTheme.typography.bodySmall.copy(color = InkSoft)
                                        )
                                    }

                                    Switch(
                                        checked = readAloudSetting,
                                        onCheckedChange = {
                                            readAloudSetting = it
                                            prefs.readAloud = it
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Ember
                                        )
                                    )
                                }
                            }
                        }

                        // Clear All Data
                        item {
                            GlassCard(
                                onClick = { showClearDataDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = Ember
                                    )
                                    Text(
                                        text = "Clear all data & reset",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Ember
                                        )
                                    )
                                }
                            }
                        }

                        // Mandatory Footer Disclaimer Note from Section 6
                        item {
                            Text(
                                text = "Selah uses AI and can make mistakes. Scripture quotations are from the King James Version (public domain). For personal crises please contact local emergency services.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = InkSoft.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }

        // Edit Name Dialog
        if (isEditingName) {
            AlertDialog(
                onDismissRequest = { isEditingName = false },
                title = { Text("Edit Name") },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        placeholder = { Text("Your name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ember
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            prefs.userName = tempName.trim()
                            isEditingName = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Ember)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isEditingName = false }) {
                        Text("Cancel", color = InkSoft)
                    }
                }
            )
        }

        // Clear Data Dialog
        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = { Text("Clear All Data?") },
                text = { Text("This will erase saved verses, bookmarks, and chat history. This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            prefs.clearAllData()
                            savedVerseIds = emptySet()
                            conversations = emptyList()
                            showClearDataDialog = false
                            Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Ember)
                    ) {
                        Text("Clear Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) {
                        Text("Cancel", color = InkSoft)
                    }
                }
            )
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (isSelected) Ink else Color.Transparent,
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else InkSoft
            ),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun EmptyStateView(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Ink
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(color = InkSoft),
            textAlign = TextAlign.Center
        )
    }
}
