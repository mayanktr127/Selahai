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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChatBubbleOutline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
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
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                icon = Icons.Default.ChatBubbleOutline,
                label = "Ask Selah",
                isSelected = currentTab == DockTab.ASK,
                testTag = "dock_tab_ask",
                onClick = { onTabSelected(DockTab.ASK) }
            )
            DockItem(
                icon = Icons.Default.PersonOutline,
                label = "Profile & Saved",
                isSelected = currentTab == DockTab.PROFILE,
                testTag = "dock_tab_profile",
                onClick = { onTabSelected(DockTab.PROFILE) }
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
            .size(46.dp)
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
            modifier = Modifier.size(24.dp)
        )
    }
}
