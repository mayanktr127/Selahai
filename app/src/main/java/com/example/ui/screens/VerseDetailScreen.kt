package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SelahPreferences
import com.example.data.VerseRepository
import com.example.ui.components.DawnBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import com.example.ui.theme.ScriptureVerseLargeStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerseDetailScreen(
    verseId: Int,
    prefs: SelahPreferences,
    onBack: () -> Unit,
    onChatAboutVerse: (String) -> Unit,
    onOpenRelatedVerse: (String) -> Unit
) {
    val context = LocalContext.current
    val verse = remember(verseId) { VerseRepository.getById(verseId) ?: VerseRepository.getVerseOfTheDay() }

    var isSaved by remember(verse.id) { mutableStateOf(prefs.isVerseSaved(verse.id)) }
    var isLiked by remember(verse.id) { mutableStateOf(prefs.getLikedVerseIds().contains(verse.id)) }

    // Collapsible accordion states
    var isSettingOpen by remember { mutableStateOf(true) }
    var isMeaningOpen by remember { mutableStateOf(true) }
    var isTodayOpen by remember { mutableStateOf(true) }
    var isDeeperOpen by remember { mutableStateOf(true) }

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
                        .testTag("verse_detail_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink
                    )
                }

                // Reference in ember pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Ember,
                    shadowElevation = 3.dp
                ) {
                    Text(
                        text = verse.reference,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Share button
                IconButton(
                    onClick = {
                        val shareText = "\"${verse.text}\"\n— ${verse.reference}\n\nReflected with Selah Bible Companion"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Verse"))
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.65f))
                        .testTag("verse_detail_share")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Ink
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Verse Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verse_detail_main_card")
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = verse.part.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmberDeep,
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "\"${verse.text}\"",
                            style = ScriptureVerseLargeStyle.copy(
                                color = Ink,
                                fontSize = 21.sp,
                                lineHeight = 32.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = verse.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = InkSoft
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Theme Tags
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            verse.themes.forEach { theme ->
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color.White.copy(alpha = 0.7f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                                ) {
                                    Text(
                                        text = "#$theme",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = InkSoft
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Collapsible Accordion: Where it's from (Setting)
                AccordionSection(
                    title = "Where it's from",
                    content = verse.setting,
                    isOpen = isSettingOpen,
                    onToggle = { isSettingOpen = !isSettingOpen },
                    testTag = "accordion_setting"
                )

                // Collapsible Accordion: What it means (Meaning)
                AccordionSection(
                    title = "What it means",
                    content = verse.meaning,
                    isOpen = isMeaningOpen,
                    onToggle = { isMeaningOpen = !isMeaningOpen },
                    testTag = "accordion_meaning"
                )

                // Collapsible Accordion: What it means for you today (Today)
                AccordionSection(
                    title = "What it means for you today",
                    content = verse.today,
                    isOpen = isTodayOpen,
                    onToggle = { isTodayOpen = !isTodayOpen },
                    testTag = "accordion_today"
                )

                // Collapsible Accordion: Go deeper (Related)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("accordion_deeper")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDeeperOpen = !isDeeperOpen },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Go deeper",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                            )

                            Icon(
                                imageVector = if (isDeeperOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = InkSoft
                            )
                        }

                        AnimatedVisibility(visible = isDeeperOpen) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text(
                                    text = "Related scriptures to study alongside this passage:",
                                    style = MaterialTheme.typography.bodySmall.copy(color = InkSoft)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    verse.related.forEach { ref ->
                                        Surface(
                                            onClick = { onOpenRelatedVerse(ref) },
                                            shape = RoundedCornerShape(999.dp),
                                            color = Dawn100,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Ember.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = ref,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmberDeep
                                                ),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Bottom Action Bar: Like | Bookmark | Primary "Chat about this verse"
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                isLiked = prefs.toggleLikedVerse(verse.id)
                                val msg = if (isLiked) "Favorited ✦" else "Removed from favorites"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) Ember else InkSoft
                            )
                        }

                        IconButton(
                            onClick = {
                                isSaved = prefs.toggleSavedVerse(verse.id)
                                val msg = if (isSaved) "Saved to library ✦" else "Removed from saved"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isSaved) Ember else InkSoft
                            )
                        }
                    }

                    // Primary Button: "Chat about this verse"
                    Button(
                        onClick = {
                            onChatAboutVerse("Help me understand ${verse.reference} deeply and how it applies to my life.")
                        },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Ember,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("chat_about_verse_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Chat about this verse",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccordionSection(
    title: String,
    content: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                )

                Icon(
                    imageVector = if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle",
                    tint = InkSoft
                )
            }

            AnimatedVisibility(visible = isOpen) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = InkSoft,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
