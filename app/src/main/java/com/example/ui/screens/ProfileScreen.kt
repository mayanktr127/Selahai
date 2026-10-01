package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SelahPreferences
import com.example.ui.components.DawnBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import com.example.ui.theme.NightBase
import com.example.ui.theme.NightSurface
import com.example.ui.theme.NightText
import com.example.ui.theme.NightTextSoft

@Composable
fun ProfileScreen(
    prefs: SelahPreferences,
    onBack: () -> Unit,
    onThemeChanged: (String) -> Unit,
    onResetToOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    var currentThemeMode by remember { mutableStateOf(prefs.themeMode) }
    val isDark = when (currentThemeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    val primaryTextColor = if (isDark) NightText else Ink
    val secondaryTextColor = if (isDark) NightTextSoft else InkSoft
    val buttonSurfaceColor = if (isDark) NightSurface else Color.White.copy(alpha = 0.65f)

    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(prefs.userName) }
    var readAloudSetting by remember { mutableStateOf(prefs.readAloud) }
    var currentGuideStyle by remember { mutableStateOf(prefs.guideStyle) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    DawnBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar: Back arrow on left, Title "Profile", Dark mode icon on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(buttonSurfaceColor)
                        .testTag("profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryTextColor
                    )
                }

                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                )

                // Dark mode toggle button: moon in light mode, sun in dark mode
                IconButton(
                    onClick = {
                        val newMode = if (isDark) "light" else "dark"
                        currentThemeMode = newMode
                        prefs.themeMode = newMode
                        onThemeChanged(newMode)
                        Toast.makeText(
                            context,
                            if (newMode == "dark") "Night Vigil enabled" else "Dawn Glass enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(buttonSurfaceColor)
                        .testTag("dark_mode_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = if (isDark) Color(0xFFFFB74D) else Ember
                    )
                }
            }

            // Profile Settings List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Your Name (editable with pencil icon)
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
                                    style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (prefs.userName.isBlank()) "Friend" else prefs.userName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = primaryTextColor
                                    )
                                )
                            }

                            IconButton(
                                onClick = {
                                    tempName = prefs.userName
                                    isEditingName = true
                                },
                                modifier = Modifier.testTag("edit_name_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = Ember)
                            }
                        }
                    }
                }

                // 2. Guide Persona (Shepherd, Scholar, Storyteller, Friend)
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Guide Persona",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
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
                                val isCurrent = currentGuideStyle == id
                                Surface(
                                    onClick = {
                                        currentGuideStyle = id
                                        prefs.guideStyle = id
                                        Toast.makeText(context, "Guide set to $label", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isCurrent) (if (isDark) Ember.copy(alpha = 0.25f) else Dawn100) else Color.Transparent,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrent) Ember else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) (if (isDark) Color(0xFFFFB74D) else EmberDeep) else primaryTextColor
                                        ),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Theme Appearance (Dawn Glass, Night Vigil, System)
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Theme Appearance",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "light" to "Dawn Glass",
                                    "dark" to "Night Vigil",
                                    "system" to "System"
                                ).forEach { (mode, name) ->
                                    val isSel = currentThemeMode == mode
                                    Surface(
                                        onClick = {
                                            currentThemeMode = mode
                                            prefs.themeMode = mode
                                            onThemeChanged(mode)
                                        },
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isSel) Ember else (if (isDark) NightBase.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.6f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = name,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else primaryTextColor
                                            ),
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Read Replies Aloud Toggle
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
                                        color = primaryTextColor
                                    )
                                )
                                Text(
                                    text = "Automatically speak Selah's reflections aloud",
                                    style = MaterialTheme.typography.bodySmall.copy(color = secondaryTextColor)
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

                // 5. Clear All Data & Reset
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

                // 6. Mandatory Disclaimer Note
                item {
                    Text(
                        text = "Selah uses AI and can make mistakes. Scripture quotations are from the King James Version (public domain). For personal crises please contact local emergency services or a crisis line.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = secondaryTextColor.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }

        // Edit Name Dialog
        if (isEditingName) {
            AlertDialog(
                onDismissRequest = { isEditingName = false },
                title = { Text("Edit Name", color = primaryTextColor) },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        placeholder = { Text("Your name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ember,
                            focusedTextColor = primaryTextColor,
                            unfocusedTextColor = primaryTextColor
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
                        Text("Cancel", color = secondaryTextColor)
                    }
                }
            )
        }

        // Clear Data Dialog
        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = { Text("Reset All Data?", color = primaryTextColor) },
                text = {
                    Text(
                        "This will delete your saved name, reset your guide persona, and clear all chat history. This action cannot be undone.",
                        color = secondaryTextColor
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            prefs.clearAllData()
                            currentGuideStyle = prefs.guideStyle
                            readAloudSetting = prefs.readAloud
                            currentThemeMode = prefs.themeMode
                            onThemeChanged(prefs.themeMode)
                            showClearDataDialog = false
                            Toast.makeText(context, "All data reset successfully", Toast.LENGTH_SHORT).show()
                            onResetToOnboarding()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Ember)
                    ) {
                        Text("Reset Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) {
                        Text("Cancel", color = secondaryTextColor)
                    }
                }
            )
        }
    }
}
