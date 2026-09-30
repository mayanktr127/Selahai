package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SelahPreferences
import com.example.data.VerseConstants
import com.example.data.VerseRepository
import com.example.ui.components.DawnBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.SelahOrb
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import com.example.ui.theme.ScriptureVerseStyle
import com.example.ui.theme.Sky400

@Composable
fun HomeScreen(
    userName: String,
    prefs: SelahPreferences,
    onNavigateToChat: (initialPrompt: String?) -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenVerseDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    var isReminderOn by remember { mutableStateOf(prefs.dailyReminder) }
    val todayVerse = remember { VerseRepository.getVerseOfTheDay() }
    var isBookmarked by remember { mutableStateOf(prefs.isVerseSaved(todayVerse.id)) }

    val scrollState = rememberScrollState()

    DawnBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header: Bell & Settings/Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selah Brand badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelahOrb(size = 32.dp, isPulsing = false)
                    Text(
                        text = "Selah",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                // Header Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(
                        onClick = {
                            isReminderOn = !isReminderOn
                            prefs.dailyReminder = isReminderOn
                            val msg = if (isReminderOn) "Daily reflection reminder enabled ✦" else "Daily reminders paused"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.65f))
                            .testTag("home_bell_button")
                    ) {
                        Icon(
                            imageVector = if (isReminderOn) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                            contentDescription = "Daily Reminder",
                            tint = if (isReminderOn) Ember else InkSoft
                        )
                    }

                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.65f))
                            .testTag("home_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Ink
                        )
                    }
                }
            }

            // Scrollable Home Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Headline: "Hi {name}," / "What's on your heart today?"
                Text(
                    text = "Hi ${if (userName.isBlank()) "Friend" else userName},",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        color = Ember,
                        fontSize = 32.sp
                    )
                )

                Text(
                    text = "What's on your heart today?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Ink,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ask anything about the Bible, or explore a verse that speaks to where you are.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = InkSoft,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mood Row (horizontal scroll of chips)
                Text(
                    text = "HOW ARE YOU FEELING?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkSoft.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VerseConstants.TOPICS.forEach { topic ->
                        Surface(
                            onClick = {
                                onNavigateToChat(topic.prompt)
                            },
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.75f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                            shadowElevation = 2.dp,
                            modifier = Modifier.testTag("mood_chip_${topic.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Ember)
                                )
                                Text(
                                    text = topic.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Verse of the Day Hero Glass Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verse_of_the_day_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Dawn100,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Ember.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "Verse of the Day",
                                    color = EmberDeep,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    isBookmarked = prefs.toggleSavedVerse(todayVerse.id)
                                    val msg = if (isBookmarked) "Saved to your library ✦" else "Removed from saved"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save Verse",
                                    tint = if (isBookmarked) Ember else InkSoft
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "\"${todayVerse.text}\"",
                            style = ScriptureVerseStyle.copy(
                                color = Ink,
                                fontSize = 18.sp,
                                lineHeight = 28.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "— ${todayVerse.reference} · ${todayVerse.title}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Ember
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = {
                                    onNavigateToChat("Help me understand ${todayVerse.reference} deeply and how it applies to my life.")
                                },
                                shape = RoundedCornerShape(999.dp),
                                color = Ember,
                                shadowElevation = 3.dp,
                                modifier = Modifier.testTag("understand_this_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Understand this",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { onOpenVerseDetail(todayVerse.id) },
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                            ) {
                                Text(
                                    text = "Read Full",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2x2 Tool Grid
                Text(
                    text = "EXPLORE SELAH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = InkSoft.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolGridCard(
                        title = "Ask Selah",
                        subtitle = "Chat about any verse or question",
                        icon = Icons.Default.ChatBubbleOutline,
                        iconTint = Ember,
                        onClick = { onNavigateToChat(null) },
                        modifier = Modifier.weight(1f),
                        testTag = "tool_ask_selah"
                    )

                    ToolGridCard(
                        title = "Verse Library",
                        subtitle = "100 verses, explained",
                        icon = Icons.Default.AutoStories,
                        iconTint = Sky400,
                        onClick = onNavigateToLibrary,
                        modifier = Modifier.weight(1f),
                        testTag = "tool_verse_library"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolGridCard(
                        title = "Voice Reflection",
                        subtitle = "Talk it through out loud",
                        icon = Icons.Default.GraphicEq,
                        iconTint = Color(0xFFB594F0),
                        onClick = onNavigateToVoice,
                        modifier = Modifier.weight(1f),
                        testTag = "tool_voice_reflection"
                    )

                    ToolGridCard(
                        title = "Saved",
                        subtitle = "Your verses & chats",
                        icon = Icons.Default.BookmarkBorder,
                        iconTint = Color(0xFFF28DAE),
                        onClick = onNavigateToSaved,
                        modifier = Modifier.weight(1f),
                        testTag = "tool_saved"
                    )
                }

                Spacer(modifier = Modifier.height(100.dp)) // Spacing for sticky search and dock
            }

            // Sticky Bottom "Ask anything..." pill input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Surface(
                    onClick = { onNavigateToChat(null) },
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("home_ask_anything_pill")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Ember,
                                modifier = Modifier.size(20.dp)
                            )

                            Text(
                                text = "Ask anything...",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    color = InkSoft
                                )
                            )
                        }

                        // Circular mic button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Ember.copy(alpha = 0.12f))
                                .clickable { onNavigateToVoice() }
                                .testTag("home_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Mode",
                                tint = Ember,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolGridCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = InkSoft.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = InkSoft,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
