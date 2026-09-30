package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassFillLightCard
import com.example.ui.theme.NightGlassBorder
import com.example.ui.theme.NightGlassFill

/**
 * Dawn Glass Card surface with frosted fill, subtle crisp highlight border,
 * and soft ambient shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val fill = backgroundColor ?: if (isDark) NightGlassFill else GlassFillLightCard
    val border = borderColor ?: if (isDark) NightGlassBorder else GlassBorderLight

    Surface(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0x26FF7850),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0x1AFF7850)
            )
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(shape)
                        .clickable(onClick = onClick)
                } else Modifier.clip(shape)
            ),
        shape = shape,
        color = fill,
        border = BorderStroke(borderWidth, border)
    ) {
        Box(content = content)
    }
}
