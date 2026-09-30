package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink

enum class DockTab {
    HOME,
    LIBRARY,
    ASK,
    PROFILE
}

@Composable
fun FloatingDock(
    currentTab: DockTab,
    onTabSelected: (DockTab) -> Unit,
    onChatbotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Main Nav Menu Bar (Home, Library, Profile)
            Row(
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(999.dp),
                        ambientColor = Color(0x33000000),
                        spotColor = Color(0x40000000)
                    )
                    .clip(RoundedCornerShape(999.dp))
                    .background(Ink)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentTab == DockTab.HOME,
                    testTag = "dock_tab_home",
                    onClick = { onTabSelected(DockTab.HOME) }
                )
                DockItem(
                    icon = Icons.Default.AutoStories,
                    label = "Library",
                    isSelected = currentTab == DockTab.LIBRARY,
                    testTag = "dock_tab_library",
                    onClick = { onTabSelected(DockTab.LIBRARY) }
                )
                DockItem(
                    icon = Icons.Default.PersonOutline,
                    label = "Profile & Saved",
                    isSelected = currentTab == DockTab.PROFILE,
                    testTag = "dock_tab_profile",
                    onClick = { onTabSelected(DockTab.PROFILE) }
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Dedicated Chatbot Floating Button placed beside the nav bar on the right side
            FloatingChatbotButton(
                onClick = onChatbotClick
            )
        }
    }
}

@Composable
private fun FloatingChatbotButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color(0x4DFF5A2C),
                spotColor = Color(0x66FF5A2C)
            )
            .size(52.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFF7A4D), Ember)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("dock_chatbot_button"),
        contentAlignment = Alignment.Center
    ) {
        // Embedded Glowing Selah Orb
        SelahOrb(
            size = 46.dp,
            isPulsing = true,
            isListening = false
        )

        // Chatbot Sparkle Badge Icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Ask Selah Chatbot",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun DockItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_bg_color"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Ink else Color.White.copy(alpha = 0.65f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_icon_color"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
