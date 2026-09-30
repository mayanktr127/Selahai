package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VerseRepository
import com.example.ui.components.DawnBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import com.example.ui.theme.ScriptureVerseStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    initialName: String = "",
    onContinue: (String) -> Unit,
    onSkip: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var showDailyVerseModal by remember { mutableStateOf(false) }
    val todayVerse = remember { VerseRepository.getVerseOfTheDay() }

    DawnBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: 3-dot step progress indicator + Daily Verse pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3-dot step progress indicator in a glass pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Ember)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Ink.copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Ink.copy(alpha = 0.2f))
                        )
                    }
                }

                // Daily Verse preview pill button
                Surface(
                    onClick = { showDailyVerseModal = true },
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("welcome_daily_verse_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Verse",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Sparkle",
                            tint = Ember,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Center Content: Headline & Name Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                // Headline: "Peace be with you," (ember italic serif) / "What should I call you?" (ink sans, 30px)
                Text(
                    text = "Peace be with you,",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        color = Ember,
                        fontSize = 32.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "What should I call you?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Ink,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Selah is a calm space to reflect on the Bible. A name helps me make our reflections personal.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = InkSoft,
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Large pill input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = "Your name",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = InkSoft.copy(alpha = 0.6f)
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(999.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                        focusedBorderColor = Ember,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("name_input")
                )
            }

            // Bottom Buttons: Skip ✦ and Continue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSkip,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = InkSoft
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("skip_button")
                ) {
                    Text(
                        text = "Skip ✦",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                }

                Button(
                    onClick = {
                        val trimmed = name.trim()
                        onContinue(if (trimmed.isEmpty()) "Friend" else trimmed)
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ember,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(54.dp)
                        .testTag("continue_button")
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        // Daily Verse Preview Bottom Sheet
        if (showDailyVerseModal) {
            ModalBottomSheet(
                onDismissRequest = { showDailyVerseModal = false },
                containerColor = Color(0xFFFFF6F1),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Ember.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Verse of the Day",
                                color = EmberDeep,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(onClick = { showDailyVerseModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = InkSoft)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "\"${todayVerse.text}\"",
                        style = ScriptureVerseStyle.copy(
                            color = Ink,
                            fontSize = 20.sp,
                            lineHeight = 30.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "— ${todayVerse.reference} (${todayVerse.title})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Ember
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = todayVerse.meaning,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = InkSoft,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
