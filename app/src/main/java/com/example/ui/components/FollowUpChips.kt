package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Ember
import com.example.ui.theme.Ink

@Composable
fun FollowUpChips(
    followups: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (followups.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        followups.forEachIndexed { index, question ->
            Surface(
                onClick = { onSelect(question) },
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, Ember.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.testTag("followup_chip_$index")
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

object FollowUpParser {
    private val regex = Regex("""<<FOLLOWUPS:\s*(.*?)\s*>>""", RegexOption.DOT_MATCHES_ALL)

    fun parse(rawText: String): Pair<String, List<String>> {
        val match = regex.find(rawText) ?: return Pair(rawText, emptyList())
        val cleanText = rawText.replace(regex, "").trim()
        val questions = match.groupValues[1]
            .split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        return Pair(cleanText, questions)
    }
}
