package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.AvailableModels
import com.example.data.LlmModel
import com.example.data.SelahPreferences
import com.example.ui.components.DawnBackground
import com.example.ui.components.SelahOrb
import com.example.ui.components.SubscriptionPlan
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft

@Composable
fun OnboardingScreen(
    prefs: SelahPreferences,
    onComplete: (name: String, guideStyle: String) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf(prefs.userName) }
    var selectedModel by remember { mutableStateOf(AvailableModels.getById(prefs.activeModelId)) }
    val selectedIntents = remember { mutableStateListOf("Peace & Calming Anxiety", "Scripture Study") }
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) } // null = Free Tier 7-min

    DawnBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation & Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    IconButton(
                        onClick = { step -= 1 },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.65f))
                            .testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(42.dp))
                }

                // 4-step Progress Pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..3) {
                            Box(
                                modifier = Modifier
                                    .size(if (i == step) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (i == step) Ember else Ink.copy(alpha = 0.2f))
                            )
                        }
                    }
                }

                // Skip / Quick Start
                Surface(
                    onClick = {
                        val finalName = if (userName.trim().isEmpty()) "Friend" else userName.trim()
                        prefs.userName = finalName
                        prefs.guideStyle = selectedModel.guideStyleKey
                        prefs.activeModelId = selectedModel.id
                        prefs.hasCompletedOnboarding = true
                        onComplete(finalName, selectedModel.guideStyleKey)
                    },
                    shape = RoundedCornerShape(999.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        text = "Skip ✦",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = InkSoft
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Step Content
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_step_content",
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                when (currentStep) {
                    0 -> OnboardingStepWelcome()
                    1 -> OnboardingStepIntent(
                        selectedIntents = selectedIntents,
                        onToggle = { intent ->
                            if (selectedIntents.contains(intent)) {
                                if (selectedIntents.size > 1) selectedIntents.remove(intent)
                            } else {
                                selectedIntents.add(intent)
                            }
                        }
                    )
                    2 -> OnboardingStepModel(
                        selectedModel = selectedModel,
                        onSelect = { selectedModel = it }
                    )
                    3 -> OnboardingStepProfileAndTier(
                        name = userName,
                        onNameChange = { userName = it },
                        selectedPlan = selectedPlan,
                        onSelectPlan = { selectedPlan = it }
                    )
                }
            }

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (step < 3) {
                            step += 1
                        } else {
                            val finalName = if (userName.trim().isEmpty()) "Friend" else userName.trim()
                            prefs.userName = finalName
                            prefs.guideStyle = selectedModel.guideStyleKey
                            prefs.activeModelId = selectedModel.id
                            prefs.hasCompletedOnboarding = true
                            if (selectedPlan != null) {
                                prefs.activateSubscription(selectedPlan!!.id)
                            }
                            onComplete(finalName, selectedModel.guideStyleKey)
                        }
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ember,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (step == 3) "Enter Sanctuary ✦" else "Continue",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepWelcome() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Animated Orb with warm aura
        Box(contentAlignment = Alignment.Center) {
            SelahOrb(size = 110.dp, isPulsing = true, isListening = false)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Selah",
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                color = Ember,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Your Bible AI Reflection Sanctuary",
            style = MaterialTheme.typography.titleLarge.copy(
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "A calm space to explore Holy Scripture, ask honest life questions, and find peace grounded in God's Word.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = InkSoft,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Highlights
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                .border(1.dp, Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingPillRow(
                icon = Icons.Default.AutoAwesome,
                title = "ChatGPT & Claude Model Switcher",
                subtitle = "Select deep contemplative, exegetical, or lightning fast tones"
            )
            OnboardingPillRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "100 Essential Biblical Verses",
                subtitle = "Complete settings, original languages, and modern application"
            )
            OnboardingPillRow(
                icon = Icons.Default.Timer,
                title = "Free 7-Minute Daily Sessions",
                subtitle = "Or unlock unlimited unhurried time with Selah Pro"
            )
        }
    }
}

@Composable
private fun OnboardingStepIntent(
    selectedIntents: List<String>,
    onToggle: (String) -> Unit
) {
    val intents = listOf(
        "Peace & Calming Anxiety",
        "Scripture Study & Context",
        "Daily Morning Reflection",
        "Wisdom for Difficult Decisions",
        "Grief, Healing & Comfort",
        "Overcoming Doubt & Questions"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "What brings you to Selah?",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 26.sp
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select what matters most right now. Selah will tailor your daily reflections and verse suggestions.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = InkSoft,
                lineHeight = 21.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            intents.forEach { intent ->
                val isSelected = selectedIntents.contains(intent)
                Surface(
                    onClick = { onToggle(intent) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Ember.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.75f),
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Ember else Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = intent,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) EmberDeep else Ink,
                                fontSize = 15.sp
                            )
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Ember),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepModel(
    selectedModel: LlmModel,
    onSelect: (LlmModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Choose Your Reflection Tone",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 26.sp
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Like ChatGPT and Claude, you can switch models at any time directly in the chat header.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = InkSoft,
                lineHeight = 21.sp
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AvailableModels.models.take(4).forEach { model ->
                val isSelected = model.id == selectedModel.id
                Surface(
                    onClick = { onSelect(model) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) Ember.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.75f),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Ember else Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Ember else Ink.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    model.id.contains("gemini") -> Icons.Default.Speed
                                    model.id.contains("gpt") -> Icons.Default.Psychology
                                    model.id.contains("storyteller") -> Icons.Default.AutoStories
                                    else -> Icons.Default.Favorite
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Ink,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = model.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Ink,
                                        fontSize = 15.sp
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Ember.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = model.speedTag,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmberDeep,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = model.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = InkSoft,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Ember),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepProfileAndTier(
    name: String,
    onNameChange: (String) -> Unit,
    selectedPlan: SubscriptionPlan?,
    onSelectPlan: (SubscriptionPlan?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = "Welcome to the Sanctuary",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 24.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "What is your name? A name helps Selah make prayers and reflections personal.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = InkSoft,
                lineHeight = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Your name (e.g. Sarah, David)", style = MaterialTheme.typography.bodyMedium.copy(color = InkSoft.copy(alpha = 0.6f))) },
            singleLine = true,
            shape = RoundedCornerShape(999.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                focusedBorderColor = Ember,
                unfocusedBorderColor = Color.White,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_name_input")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Choose Your Reflection Plan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Ink,
                fontSize = 15.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Free Option
        val isFreeSelected = selectedPlan == null
        Surface(
            onClick = { onSelectPlan(null) },
            shape = RoundedCornerShape(16.dp),
            color = if (isFreeSelected) Ember.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.75f),
            border = BorderStroke(if (isFreeSelected) 2.dp else 1.dp, if (isFreeSelected) Ember else Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Free Daily Plan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Ink, fontSize = 14.sp))
                    Text("5–7 minutes of daily AI chat reflection", style = MaterialTheme.typography.bodySmall.copy(color = InkSoft, fontSize = 12.sp))
                }
                Text("Free", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = if (isFreeSelected) EmberDeep else Ink))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pro Monthly Option ($7.99/mo)
        val isMonthly = selectedPlan == SubscriptionPlan.MONTHLY
        Surface(
            onClick = { onSelectPlan(SubscriptionPlan.MONTHLY) },
            shape = RoundedCornerShape(16.dp),
            color = if (isMonthly) Ember.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.75f),
            border = BorderStroke(if (isMonthly) 2.dp else 1.dp, if (isMonthly) Ember else Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Monthly Reflection", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Ink, fontSize = 14.sp))
                    Text("Unlimited chats, billed monthly ($7.99/mo)", style = MaterialTheme.typography.bodySmall.copy(color = InkSoft, fontSize = 12.sp))
                }
                Text("$7.99/mo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = if (isMonthly) EmberDeep else Ink))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pro Yearly Option
        val isYearly = selectedPlan == SubscriptionPlan.YEARLY
        Surface(
            onClick = { onSelectPlan(SubscriptionPlan.YEARLY) },
            shape = RoundedCornerShape(16.dp),
            color = if (isYearly) Ember.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.75f),
            border = BorderStroke(if (isYearly) 2.dp else 1.dp, if (isYearly) Ember else Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Selah Pro Yearly", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Ink, fontSize = 14.sp))
                        Surface(shape = RoundedCornerShape(999.dp), color = Ember) {
                            Text("SAVE 22%", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                        }
                    }
                    Text("Unlimited chats, all models ($6.25/mo)", style = MaterialTheme.typography.bodySmall.copy(color = InkSoft, fontSize = 12.sp))
                }
                Text("$75/yr", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = if (isYearly) EmberDeep else Ink))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lifetime Option
        val isLifetime = selectedPlan == SubscriptionPlan.LIFETIME
        Surface(
            onClick = { onSelectPlan(SubscriptionPlan.LIFETIME) },
            shape = RoundedCornerShape(16.dp),
            color = if (isLifetime) Ember.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.75f),
            border = BorderStroke(if (isLifetime) 2.dp else 1.dp, if (isLifetime) Ember else Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Lifetime Eternal Access", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Ink, fontSize = 14.sp))
                    Text("Pay once • Forever access • All models", style = MaterialTheme.typography.bodySmall.copy(color = InkSoft, fontSize = 12.sp))
                }
                Text("$100", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = if (isLifetime) EmberDeep else Ink))
            }
        }
    }
}

@Composable
private fun OnboardingPillRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Ember.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Ember,
                modifier = Modifier.size(18.dp)
            )
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontSize = 13.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = InkSoft,
                    fontSize = 11.sp
                )
            )
        }
    }
}
