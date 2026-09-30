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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.SelahPreferences
import com.example.ui.components.DockTab
import com.example.ui.components.FloatingDock
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ChooseGuideScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SavedProfileScreen
import com.example.ui.screens.VerseDetailScreen
import com.example.ui.screens.VoiceReflectionScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.SelahTheme

enum class ScreenState {
    WELCOME,
    CHOOSE_GUIDE,
    HOME,
    CHAT,
    VOICE,
    LIBRARY,
    VERSE_DETAIL,
    SAVED_PROFILE
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
    // If user name exists, start on HOME; otherwise start on WELCOME onboarding
    var currentScreen by remember {
        mutableStateOf(if (prefs.userName.isNotBlank()) ScreenState.HOME else ScreenState.WELCOME)
    }

    var activeDockTab by remember { mutableStateOf(DockTab.HOME) }
    var activeVerseDetailId by remember { mutableIntStateOf(1) }
    var activeChatPrompt by remember { mutableStateOf<String?>(null) }
    var activeChatConversationId by remember { mutableStateOf<String?>(null) }

    // Screen back handling
    BackHandler(enabled = currentScreen != ScreenState.HOME && currentScreen != ScreenState.WELCOME) {
        when (currentScreen) {
            ScreenState.CHOOSE_GUIDE -> currentScreen = ScreenState.WELCOME
            ScreenState.CHAT, ScreenState.VOICE, ScreenState.VERSE_DETAIL -> currentScreen = ScreenState.HOME
            ScreenState.LIBRARY, ScreenState.SAVED_PROFILE -> {
                activeDockTab = DockTab.HOME
                currentScreen = ScreenState.HOME
            }
            else -> currentScreen = ScreenState.HOME
        }
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
                        WelcomeScreen(
                            initialName = prefs.userName,
                            onContinue = { name ->
                                prefs.userName = name
                                currentScreen = ScreenState.CHOOSE_GUIDE
                            },
                            onSkip = {
                                if (prefs.userName.isBlank()) prefs.userName = "Friend"
                                currentScreen = ScreenState.CHOOSE_GUIDE
                            }
                        )
                    }

                    ScreenState.CHOOSE_GUIDE -> {
                        ChooseGuideScreen(
                            currentGuide = prefs.guideStyle,
                            onBack = { currentScreen = ScreenState.WELCOME },
                            onGuideChosen = { guide ->
                                prefs.guideStyle = guide
                                currentScreen = ScreenState.HOME
                                activeDockTab = DockTab.HOME
                            }
                        )
                    }

                    ScreenState.HOME -> {
                        HomeScreen(
                            userName = prefs.userName,
                            prefs = prefs,
                            onNavigateToChat = { prompt ->
                                activeChatPrompt = prompt
                                activeChatConversationId = null
                                currentScreen = ScreenState.CHAT
                            },
                            onNavigateToVoice = {
                                currentScreen = ScreenState.VOICE
                            },
                            onNavigateToLibrary = {
                                activeDockTab = DockTab.LIBRARY
                                currentScreen = ScreenState.LIBRARY
                            },
                            onNavigateToSaved = {
                                activeDockTab = DockTab.PROFILE
                                currentScreen = ScreenState.SAVED_PROFILE
                            },
                            onNavigateToProfile = {
                                activeDockTab = DockTab.PROFILE
                                currentScreen = ScreenState.SAVED_PROFILE
                            },
                            onOpenVerseDetail = { verseId ->
                                activeVerseDetailId = verseId
                                currentScreen = ScreenState.VERSE_DETAIL
                            }
                        )
                    }

                    ScreenState.CHAT -> {
                        ChatScreen(
                            initialPrompt = activeChatPrompt,
                            conversationId = activeChatConversationId,
                            prefs = prefs,
                            onBack = { currentScreen = ScreenState.HOME },
                            onNavigateToVoice = { currentScreen = ScreenState.VOICE },
                            onOpenVerseDetail = { verseId ->
                                activeVerseDetailId = verseId
                                currentScreen = ScreenState.VERSE_DETAIL
                            }
                        )
                    }

                    ScreenState.VOICE -> {
                        VoiceReflectionScreen(
                            prefs = prefs,
                            onBack = { currentScreen = ScreenState.HOME },
                            onNavigateToTextChat = { prompt ->
                                activeChatPrompt = prompt
                                activeChatConversationId = null
                                currentScreen = ScreenState.CHAT
                            }
                        )
                    }

                    ScreenState.LIBRARY -> {
                        LibraryScreen(
                            onBack = {
                                activeDockTab = DockTab.HOME
                                currentScreen = ScreenState.HOME
                            },
                            onOpenVerseDetail = { verseId ->
                                activeVerseDetailId = verseId
                                currentScreen = ScreenState.VERSE_DETAIL
                            }
                        )
                    }

                    ScreenState.VERSE_DETAIL -> {
                        VerseDetailScreen(
                            verseId = activeVerseDetailId,
                            prefs = prefs,
                            onBack = { currentScreen = ScreenState.HOME },
                            onChatAboutVerse = { prompt ->
                                activeChatPrompt = prompt
                                activeChatConversationId = null
                                currentScreen = ScreenState.CHAT
                            },
                            onOpenRelatedVerse = { ref ->
                                val related = com.example.data.VerseRepository.getByReference(ref)
                                if (related != null) {
                                    activeVerseDetailId = related.id
                                } else {
                                    activeChatPrompt = "Tell me about $ref"
                                    activeChatConversationId = null
                                    currentScreen = ScreenState.CHAT
                                }
                            }
                        )
                    }

                    ScreenState.SAVED_PROFILE -> {
                        SavedProfileScreen(
                            prefs = prefs,
                            onBack = {
                                activeDockTab = DockTab.HOME
                                currentScreen = ScreenState.HOME
                            },
                            onOpenVerseDetail = { verseId ->
                                activeVerseDetailId = verseId
                                currentScreen = ScreenState.VERSE_DETAIL
                            },
                            onResumeChat = { convId ->
                                activeChatConversationId = convId
                                activeChatPrompt = null
                                currentScreen = ScreenState.CHAT
                            },
                            onThemeChanged = onThemeChanged
                        )
                    }
                }
            }

            // Floating Dock Navigation Bar (shown on HOME, LIBRARY, SAVED_PROFILE)
            if (currentScreen == ScreenState.HOME || currentScreen == ScreenState.LIBRARY || currentScreen == ScreenState.SAVED_PROFILE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    FloatingDock(
                        currentTab = activeDockTab,
                        onTabSelected = { tab ->
                            activeDockTab = tab
                            when (tab) {
                                DockTab.HOME -> currentScreen = ScreenState.HOME
                                DockTab.LIBRARY -> currentScreen = ScreenState.LIBRARY
                                DockTab.ASK -> {
                                    activeChatPrompt = null
                                    activeChatConversationId = null
                                    currentScreen = ScreenState.CHAT
                                }
                                DockTab.PROFILE -> currentScreen = ScreenState.SAVED_PROFILE
                            }
                        }
                    )
                }
            }
        }
    }
}
