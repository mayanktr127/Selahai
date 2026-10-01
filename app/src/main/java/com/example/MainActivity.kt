package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.SelahPreferences
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VoiceReflectionScreen
import com.example.ui.theme.SelahTheme

enum class ScreenState {
    WELCOME,
    CHAT,
    VOICE,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs = remember { SelahPreferences(this) }
            var themeMode by remember { mutableStateOf(prefs.themeMode) }

            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            SelahTheme(darkTheme = isDark) {
                SelahApp(
                    prefs = prefs,
                    onThemeChanged = { themeMode = it }
                )
            }
        }
    }
}

@Composable
fun SelahApp(
    prefs: SelahPreferences,
    onThemeChanged: (String) -> Unit
) {
    // After onboarding, the app opens directly to the chat screen (like ChatGPT and Claude)
    var currentScreen by remember {
        mutableStateOf(if (prefs.hasCompletedOnboarding) ScreenState.CHAT else ScreenState.WELCOME)
    }

    var activeChatPrompt by remember { mutableStateOf<String?>(null) }
    var activeChatConversationId by remember { mutableStateOf<String?>(null) }

    // Screen back handling: back always returns to CHAT (or exits if at CHAT/WELCOME)
    BackHandler(enabled = currentScreen != ScreenState.CHAT && currentScreen != ScreenState.WELCOME) {
        currentScreen = ScreenState.CHAT
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Desktop / Tablet width constraint (centered phone column)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transitions"
            ) { screen ->
                when (screen) {
                    ScreenState.WELCOME -> {
                        OnboardingScreen(
                            prefs = prefs,
                            onComplete = { name, guideStyle ->
                                prefs.userName = name
                                prefs.guideStyle = guideStyle
                                prefs.hasCompletedOnboarding = true
                                currentScreen = ScreenState.CHAT
                            }
                        )
                    }

                    ScreenState.CHAT -> {
                        ChatScreen(
                            initialPrompt = activeChatPrompt,
                            conversationId = activeChatConversationId,
                            prefs = prefs,
                            onNavigateToVoice = { currentScreen = ScreenState.VOICE },
                            onNavigateToProfile = { currentScreen = ScreenState.PROFILE }
                        )
                    }

                    ScreenState.VOICE -> {
                        VoiceReflectionScreen(
                            prefs = prefs,
                            onBack = { currentScreen = ScreenState.CHAT },
                            onNavigateToTextChat = { prompt ->
                                activeChatPrompt = prompt
                                activeChatConversationId = null
                                currentScreen = ScreenState.CHAT
                            }
                        )
                    }

                    ScreenState.PROFILE -> {
                        ProfileScreen(
                            prefs = prefs,
                            onBack = { currentScreen = ScreenState.CHAT },
                            onThemeChanged = onThemeChanged,
                            onResetToOnboarding = {
                                currentScreen = ScreenState.WELCOME
                            }
                        )
                    }
                }
            }
        }
    }
}
