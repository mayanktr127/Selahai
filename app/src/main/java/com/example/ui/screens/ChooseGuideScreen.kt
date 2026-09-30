package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DawnBackground
import com.example.ui.components.SelahOrb
import com.example.ui.components.SlideToStart
import com.example.ui.theme.Ember
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import com.example.ui.theme.LavenderWash
import com.example.ui.theme.RoseWash
import com.example.ui.theme.Sky400

data class GuideProfile(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    val tintColor: Color,
    val description: String
)

val guidesList = listOf(
    GuideProfile(
        id = "scholar",
        name = "Scholar",
        subtitle = "History, context & original languages",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        tintColor = Sky400,
        description = "Provides rich historical setting, original Greek and Hebrew nuances, and fair theological perspectives across church traditions."
    ),
    GuideProfile(
        id = "shepherd",
        name = "Shepherd",
        subtitle = "Gentle comfort & prayer",
        icon = Icons.Default.Favorite,
        tintColor = Ember,
        description = "Offers pastoral gentleness, emotional reassurance, heartfelt encouragement, and finishes with a peaceful prayer when appropriate."
    ),
    GuideProfile(
        id = "storyteller",
        name = "Storyteller",
        subtitle = "The story behind every verse",
        icon = Icons.Default.AutoStories,
        tintColor = Color(0xFFB594F0),
        description = "Brings the narrative of the Bible to life, unfolding the journeys, struggles, and human lives surrounding each passage."
    ),
    GuideProfile(
        id = "friend",
        name = "Friend",
        subtitle = "Casual, modern, everyday life",
        icon = Icons.Default.ChatBubbleOutline,
        tintColor = Color(0xFFF28DAE),
        description = "Converses casually and warmly with short, encouraging thoughts and relatable modern examples for work, friends, and everyday life."
    )
)

@Composable
fun ChooseGuideScreen(
    currentGuide: String = "shepherd",
    onBack: () -> Unit,
    onGuideChosen: (String) -> Unit
) {
    var selectedIndex by remember {
        val initialIdx = guidesList.indexOfFirst { it.id == currentGuide }.coerceAtLeast(1) // Shepherd default
        mutableIntStateOf(initialIdx)
    }

    val guide = guidesList[selectedIndex]

    DawnBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.65f))
                        .testTag("choose_guide_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                ) {
                    Text(
                        text = "Choose your guide",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }

                // 3-dot step progress indicator (Step 2 active)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Ink.copy(alpha = 0.2f)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Ember))
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Ink.copy(alpha = 0.2f)))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arc Carousel / Guide Orbs Row
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    guidesList.forEachIndexed { index, g ->
                        val isSelected = index == selectedIndex
                        val orbSize = if (isSelected) 100.dp else 56.dp
                        val alpha = if (isSelected) 1f else 0.5f

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedIndex = index }
                                .padding(4.dp)
                                .testTag("guide_orb_$index")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                SelahOrb(
                                    size = orbSize,
                                    tintColor = g.tintColor,
                                    isListening = isSelected,
                                    isPulsing = isSelected
                                )

                                Icon(
                                    imageVector = g.icon,
                                    contentDescription = g.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isSelected) 36.dp else 22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = g.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Ink else InkSoft.copy(alpha = alpha)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Detail of current guide
                AnimatedContent(
                    targetState = guide,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "guide_detail"
                ) { targetGuide ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Meet the ",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = Ink,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = targetGuide.name,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    color = targetGuide.tintColor,
                                    fontSize = 30.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = targetGuide.subtitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = InkSoft
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = targetGuide.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = InkSoft,
                                lineHeight = 22.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom: Slide to Begin track
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SlideToStart(
                    label = "Slide to begin ✦",
                    onComplete = {
                        onGuideChosen(guide.id)
                    }
                )
            }
        }
    }
}
