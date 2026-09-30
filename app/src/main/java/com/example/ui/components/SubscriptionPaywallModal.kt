package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SelahPreferences
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft

enum class SubscriptionPlan(
    val id: String,
    val title: String,
    val price: String,
    val period: String,
    val badge: String?,
    val note: String
) {
    MONTHLY(
        id = "monthly",
        title = "Monthly Reflection",
        price = "$7.99",
        period = "/ month",
        badge = null,
        note = "Billed monthly • Cancel anytime"
    ),
    YEARLY(
        id = "yearly",
        title = "Yearly Sanctuary",
        price = "$75",
        period = "/ year",
        badge = "MOST POPULAR • SAVE 22%",
        note = "Just $6.25/mo • Includes 7-day free trial"
    ),
    LIFETIME(
        id = "lifetime",
        title = "Lifetime Eternal Access",
        price = "$100",
        period = "one-time",
        badge = "BEST VALUE • PAY ONCE",
        note = "Forever access to all LLMs, verses & updates"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPaywallModal(
    prefs: SelahPreferences,
    onDismiss: () -> Unit,
    onSubscribed: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.YEARLY) }
    var isSuccess by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFFFF9F5),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Ember.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Ember.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Ember,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Free Session Limit Reached",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmberDeep
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("paywall_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = InkSoft
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selah Orb Icon with Glow
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFF8A5B), Ember, Color(0xFFC73D17))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Title
            Text(
                text = "Deepen Your Walk with God",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Free tier allows 5–7 minutes of daily reflection. Upgrade to unlock unlimited, unhurried time in Scripture.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = InkSoft,
                    lineHeight = 21.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Benefits Checklist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BenefitItem("Unlimited Chat Time", "No 5–7 minute timer — reflect as long as your spirit needs")
                BenefitItem("All AI LLM Models", "Switch between Claude 3.5 Sonnet, GPT-4o Scholar & Gemini Flash")
                BenefitItem("Complete Previous Chats", "Search, save, and resume your entire spiritual conversation history")
                BenefitItem("Full Offline 100 Verses", "Explanations, original Greek/Hebrew settings, and daily application")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3 Subscription Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubscriptionPlan.values().forEach { plan ->
                    val isSelected = selectedPlan == plan
                    PlanCard(
                        plan = plan,
                        isSelected = isSelected,
                        onClick = { selectedPlan = plan }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button
            Button(
                onClick = {
                    prefs.activateSubscription(selectedPlan.id)
                    isSuccess = true
                    onSubscribed()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("subscribe_button"),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ember,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (selectedPlan) {
                            SubscriptionPlan.MONTHLY -> "Start Monthly • $7.99/mo"
                            SubscriptionPlan.YEARLY -> "Start Yearly • $75/year"
                            SubscriptionPlan.LIFETIME -> "Get Lifetime Access • $100"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Options: Reset Free Demo Timer & Restore
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        prefs.resetFreeTimer()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("reset_free_timer_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp), tint = InkSoft)
                        Text("Reset Free 7-Min Demo", style = MaterialTheme.typography.labelSmall.copy(color = InkSoft))
                    }
                }

                TextButton(
                    onClick = {
                        prefs.activateSubscription("yearly")
                        onSubscribed()
                    }
                ) {
                    Text("Restore Purchase", style = MaterialTheme.typography.labelSmall.copy(color = InkSoft))
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Ember else Color.White,
        label = "plan_border"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Ember.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.75f),
        label = "plan_bg"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_card_${plan.id}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Optional Badge Row
            if (plan.badge != null) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSelected) Ember else Ink.copy(alpha = 0.08f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = plan.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Ink,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    )
                    Text(
                        text = plan.note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkSoft,
                            fontSize = 12.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = plan.price,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) EmberDeep else Ink,
                            fontSize = 22.sp
                        )
                    )
                    Text(
                        text = " " + plan.period,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkSoft,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Ember.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Ember,
                modifier = Modifier.size(13.dp)
            )
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontSize = 13.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = InkSoft,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
