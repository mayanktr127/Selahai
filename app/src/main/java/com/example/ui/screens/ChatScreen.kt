package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiChatService
import com.example.data.ChatConversation
import com.example.data.ChatMessage
import com.example.data.SelahPreferences
import com.example.data.VerseRepository
import com.example.ui.components.ChatSideDrawerContent
import com.example.ui.components.DawnBackground
import com.example.ui.components.FollowUpChips
import com.example.ui.components.FollowUpParser
import com.example.ui.components.SelahOrb
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import com.example.ui.theme.NightBase
import com.example.ui.theme.NightSurface
import com.example.ui.theme.NightText
import com.example.ui.theme.NightTextSoft
import com.example.ui.theme.ScriptureVerseStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    initialPrompt: String? = null,
    conversationId: String? = null,
    prefs: SelahPreferences,
    onNavigateToVoice: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (prefs.themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    val primaryTextColor = if (isDark) NightText else Ink
    val secondaryTextColor = if (isDark) NightTextSoft else InkSoft

    var inputQuery by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    var streamJob by remember { mutableStateOf<Job?>(null) }
    var activeConversationId by remember { mutableStateOf(conversationId ?: UUID.randomUUID().toString()) }
    var currentGuideStyle by remember { mutableStateOf(prefs.guideStyle) }

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var selectedMessageForActions by remember { mutableStateOf<ChatMessage?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // TTS Setup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.getDefault()
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    // Speech-to-text dictation launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputQuery = if (inputQuery.isBlank()) spokenText else "$inputQuery $spokenText"
            }
        }
    }

    fun startDictation() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Selah…")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice dictation is not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to start a fresh chat
    fun startNewChat() {
        streamJob?.cancel()
        isStreaming = false
        activeConversationId = UUID.randomUUID().toString()
        messages.clear()
        inputQuery = ""
    }

    // Function to load a saved conversation
    fun loadConversation(convId: String) {
        streamJob?.cancel()
        isStreaming = false
        activeConversationId = convId
        val conv = prefs.getConversations().find { it.id == convId }
        if (conv != null) {
            messages.clear()
            messages.addAll(conv.messages)
            currentGuideStyle = conv.guideStyle
        }
    }

    // Load initial conversation if provided
    LaunchedEffect(conversationId) {
        if (conversationId != null) {
            loadConversation(conversationId)
        }
    }

    fun sendMessage(queryText: String) {
        val trimmed = queryText.trim()
        if (trimmed.isEmpty()) return

        // Cancel previous in-flight job if user taps a new question or suggestion
        streamJob?.cancel()
        // Clean up any stale blank placeholder messages from prior unfinished streams
        messages.removeAll { it.sender == "selah" && it.text.isBlank() }

        val userMsg = ChatMessage(sender = "user", text = trimmed)
        messages.add(userMsg)
        inputQuery = ""

        isStreaming = true
        val aiMsgId = UUID.randomUUID().toString()
        val aiMsg = ChatMessage(id = aiMsgId, sender = "selah", text = "")
        messages.add(aiMsg)

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }

        streamJob = scope.launch {
            val fullBuilder = StringBuilder()
            try {
                GeminiChatService.streamChat(
                    conversationHistory = messages.filter { it.text.isNotEmpty() },
                    userName = prefs.userName,
                    guideStyle = currentGuideStyle
                ) { chunk ->
                    fullBuilder.append(chunk)
                    val idx = messages.indexOfFirst { it.id == aiMsgId }
                    if (idx >= 0) {
                        val (cleanText, followups) = FollowUpParser.parse(fullBuilder.toString())
                        messages[idx] = messages[idx].copy(text = cleanText, followups = followups)
                    }
                }
            } catch (e: Exception) {
                val idx = messages.indexOfFirst { it.id == aiMsgId }
                if (idx >= 0) {
                    messages[idx] = messages[idx].copy(
                        text = "Peace be with you. Let us reflect on what God's Word says about this."
                    )
                }
            } finally {
                // Ensure the message is never left empty
                val idx = messages.indexOfFirst { it.id == aiMsgId }
                if (idx >= 0 && messages[idx].text.isBlank()) {
                    messages[idx] = messages[idx].copy(
                        text = "Peace be with you. Let us pause and reflect on God's unfailing love and grace for your life today.\n\n<<FOLLOWUPS: What does John 3:16 mean? | How do I deal with anxiety? | Explain grace like I'm new to this>>"
                    )
                }

                isStreaming = false
                val finalReply = fullBuilder.toString().ifBlank { messages.getOrNull(idx)?.text ?: "" }
                if (prefs.readAloud && finalReply.isNotBlank()) {
                    tts?.speak(FollowUpParser.parse(finalReply).first, TextToSpeech.QUEUE_FLUSH, null, null)
                }
                // Save conversation to recents history
                prefs.saveConversation(
                    ChatConversation(
                        id = activeConversationId,
                        title = messages.firstOrNull { it.sender == "user" }?.text?.take(45) ?: "Reflection",
                        guideStyle = currentGuideStyle,
                        lastUpdated = System.currentTimeMillis(),
                        messages = messages.filter { it.text.isNotBlank() }
                    )
                )
            }
        }
    }

    // Send initial prompt if provided
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank() && messages.isEmpty()) {
            sendMessage(initialPrompt)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDark) NightSurface else Color(0xFFFFF9F5),
                drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                modifier = Modifier.width(310.dp)
            ) {
                ChatSideDrawerContent(
                    prefs = prefs,
                    activeConversationId = activeConversationId,
                    onSelectConversation = { convId ->
                        scope.launch { drawerState.close() }
                        loadConversation(convId)
                    },
                    onNewChat = {
                        scope.launch { drawerState.close() }
                        startNewChat()
                    },
                    onOpenProfile = {
                        scope.launch { drawerState.close() }
                        onNavigateToProfile()
                    },
                    onDeleteConversation = { convId ->
                        prefs.deleteConversation(convId)
                        if (activeConversationId == convId) {
                            startNewChat()
                        }
                    }
                )
            }
        }
    ) {
        DawnBackground {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar: Menu (hamburger) on left, New-chat on right. Plain, clean background.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("chat_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation Menu",
                            tint = primaryTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { startNewChat() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("chat_new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "New Chat",
                            tint = primaryTextColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Middle: Empty State or Messages List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        // Empty State: Small Selah Orb (~48px) + "Hi {name}, what's on your heart?" in serif font. Nothing else.
                        val displayName = if (prefs.userName.isNotBlank()) prefs.userName else "Friend"
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                SelahOrb(size = 48.dp, isPulsing = true)
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = "Hi $displayName, what's on your heart?",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Normal,
                                        color = primaryTextColor,
                                        fontSize = 21.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Messages List in existing chat bubble style
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            itemsIndexed(messages) { index, msg ->
                                if (index == 0 || (msg.timestamp - messages[index - 1].timestamp) > 300000) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isDark) NightSurface.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp)),
                                                style = MaterialTheme.typography.labelSmall.copy(color = secondaryTextColor),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                MessageBubble(
                                    message = msg,
                                    isDark = isDark,
                                    isStreamingLast = isStreaming && index == messages.size - 1 && msg.sender == "selah",
                                    onReferenceClick = { ref ->
                                        // Requirement 4: make references send "Explain {reference}" as a new message
                                        sendMessage("Explain $ref")
                                    },
                                    onFollowUpClick = { q -> sendMessage(q) },
                                    onCopy = { text ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Selah Message", text))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    onSpeak = { text ->
                                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                                    },
                                    onRegenerate = {
                                        val lastUserQuery = messages.lastOrNull { it.sender == "user" }?.text
                                        if (!lastUserQuery.isNullOrBlank()) {
                                            if (messages.lastOrNull()?.sender == "selah") {
                                                messages.removeAt(messages.size - 1)
                                            }
                                            if (messages.lastOrNull()?.sender == "user") {
                                                messages.removeAt(messages.size - 1)
                                            }
                                            sendMessage(lastUserQuery)
                                        }
                                    },
                                    onShare = { text ->
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, text)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Reflection"))
                                    },
                                    onLike = {
                                        Toast.makeText(context, "Saved reflection! ✦", Toast.LENGTH_SHORT).show()
                                    },
                                    onLongPress = { selectedMessageForActions = msg }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }

                // Bottom: One rounded input container pinned to bottom. Respects safe area & moves up with keyboard.
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    shape = RoundedCornerShape(999.dp),
                    color = if (isDark) NightSurface else Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.White),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            placeholder = {
                                Text(
                                    text = "Ask Selah…",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = secondaryTextColor)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field")
                        )

                        // On the right: mic icon for dictation
                        IconButton(
                            onClick = { startDictation() },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("chat_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Dictate",
                                tint = secondaryTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Round voice button (waveform) -> changes to send (arrow) when text is typed -> changes to stop when streaming
                        if (isStreaming) {
                            IconButton(
                                onClick = {
                                    streamJob?.cancel()
                                    isStreaming = false
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Ember)
                                    .testTag("chat_stop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else if (inputQuery.isNotBlank()) {
                            IconButton(
                                onClick = { sendMessage(inputQuery) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Ember)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onNavigateToVoice,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Ember.copy(alpha = 0.25f) else Ember.copy(alpha = 0.12f))
                                    .testTag("chat_voice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Voice Reflection",
                                    tint = Ember,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Long Press Actions Sheet
    selectedMessageForActions?.let { msg ->
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForActions = null },
            containerColor = if (isDark) NightSurface else Color(0xFFFFF6F1),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Message Options",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                )

                // Copy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Selah Message", msg.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            selectedMessageForActions = null
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = primaryTextColor)
                    Text("Copy message", style = MaterialTheme.typography.bodyLarge.copy(color = primaryTextColor))
                }

                // Read Aloud
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            tts?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, null)
                            selectedMessageForActions = null
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = primaryTextColor)
                    Text("Read aloud", style = MaterialTheme.typography.bodyLarge.copy(color = primaryTextColor))
                }

                // Share
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, msg.text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Reflection"))
                            selectedMessageForActions = null
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = primaryTextColor)
                    Text("Share reflection", style = MaterialTheme.typography.bodyLarge.copy(color = primaryTextColor))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isDark: Boolean,
    isStreamingLast: Boolean,
    onReferenceClick: (String) -> Unit,
    onFollowUpClick: (String) -> Unit,
    onCopy: (String) -> Unit,
    onSpeak: (String) -> Unit,
    onRegenerate: () -> Unit,
    onShare: (String) -> Unit,
    onLike: () -> Unit,
    onLongPress: () -> Unit
) {
    val isUser = message.sender == "user"
    val primaryTextColor = if (isDark) NightText else Ink
    val secondaryTextColor = if (isDark) NightTextSoft else InkSoft

    if (isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 330.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .background(Brush.linearGradient(listOf(Color(0xFFFF7A4D), Ember)))
                    .clickable { onLongPress() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        return
    }

    // Thinking indicator if tokens haven't arrived yet
    if (message.text.isBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 6.dp)
        ) {
            SelahOrb(size = 26.dp, isListening = true)
            Text(
                text = "Selah is reflecting…",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = secondaryTextColor,
                    fontStyle = FontStyle.Italic
                )
            )
        }
        return
    }

    // Assistant message bubble
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            SelahOrb(
                size = 28.dp,
                isPulsing = false,
                modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 350.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 22.dp,
                            topEnd = 22.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 22.dp
                        )
                    )
                    .background(if (isDark) NightSurface else Color.White.copy(alpha = 0.92f))
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable { onLongPress() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                FormattedSelahMessage(
                    text = message.text + (if (isStreamingLast) " ▍" else ""),
                    isDark = isDark,
                    onReferenceClick = onReferenceClick
                )
            }
        }

        // Action Toolbar underneath assistant message
        if (!isStreamingLast && message.text.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp, top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onCopy(message.text) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { onSpeak(message.text) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Read aloud",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onRegenerate,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate answer",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = { onShare(message.text) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share reflection",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onLike,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Helpful",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Follow up chips
        if (!isUser && message.followups.isNotEmpty()) {
            FollowUpChips(
                followups = message.followups,
                onSelect = onFollowUpClick,
                modifier = Modifier.padding(start = 36.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun FormattedSelahMessage(
    text: String,
    isDark: Boolean,
    onReferenceClick: (String) -> Unit
) {
    val references = remember(text) { VerseRepository.findReferencesInText(text) }
    val lines = text.split("\n")
    val primaryTextColor = if (isDark) NightText else Ink
    val secondaryTextColor = if (isDark) NightTextSoft else InkSoft

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith(">") -> {
                    // Blockquote verse card style with left ember border
                    val quote = trimmed.removePrefix(">").trim()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) NightBase.copy(alpha = 0.6f) else Dawn100.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = Ember.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(Ember)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = quote,
                                style = ScriptureVerseStyle.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = primaryTextColor
                                )
                            )
                        }
                    }
                }
                trimmed.startsWith("###") -> {
                    Text(
                        text = trimmed.removePrefix("###").trim(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFB74D) else EmberDeep
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                else -> {
                    if (trimmed.isNotEmpty()) {
                        val matchedRef = references.find { trimmed.contains(it) }
                        if (matchedRef != null) {
                            val annotated = buildAnnotatedString {
                                val startIdx = trimmed.indexOf(matchedRef)
                                append(trimmed.substring(0, startIdx))
                                withStyle(
                                    SpanStyle(
                                        color = Ember,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append(matchedRef)
                                }
                                append(trimmed.substring(startIdx + matchedRef.length))
                            }
                            Text(
                                text = annotated,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = primaryTextColor,
                                    lineHeight = 22.sp
                                ),
                                modifier = Modifier.clickable { onReferenceClick(matchedRef) }
                            )
                        } else {
                            Text(
                                text = trimmed,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = primaryTextColor,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
