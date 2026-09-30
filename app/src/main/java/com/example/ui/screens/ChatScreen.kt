package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalDensity
import com.example.data.AvailableModels
import com.example.data.LlmModel
import com.example.ui.components.ChatHistorySheet
import com.example.ui.components.ModelSelectorPill
import com.example.ui.components.ModelSwitcherSheet
import com.example.ui.components.SubscriptionPaywallModal
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiChatService
import com.example.data.ChatConversation
import com.example.data.ChatMessage
import com.example.data.SelahPreferences
import com.example.data.VerseRepository
import com.example.ui.components.DawnBackground
import com.example.ui.components.FollowUpChips
import com.example.ui.components.FollowUpParser
import com.example.ui.components.GlassCard
import com.example.ui.components.SelahOrb
import com.example.ui.theme.Dawn100
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
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
    onBack: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onOpenVerseDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputQuery by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    var streamJob by remember { mutableStateOf<Job?>(null) }
    var activeConversationId by remember { mutableStateOf(conversationId ?: UUID.randomUUID().toString()) }
    var currentGuideStyle by remember { mutableStateOf(prefs.guideStyle) }
    var isPremium by remember { mutableStateOf(prefs.isPremium) }
    var freeSecondsLeft by remember { mutableIntStateOf(prefs.freeSecondsRemaining) }
    var activeModel by remember { mutableStateOf(AvailableModels.getById(prefs.activeModelId)) }

    var showModelSwitcher by remember { mutableStateOf(false) }
    var showChatHistory by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }

    // Live Session Timer Countdown for Free Tier (5-7 mins total session limit)
    LaunchedEffect(isPremium, freeSecondsLeft) {
        if (!isPremium && freeSecondsLeft > 0) {
            while (freeSecondsLeft > 0) {
                kotlinx.coroutines.delay(1000L)
                freeSecondsLeft -= 1
                prefs.freeSecondsRemaining = freeSecondsLeft
            }
            showPaywall = true
        }
    }

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var selectedMessageForActions by remember { mutableStateOf<ChatMessage?>(null) }
    var showMenu by remember { mutableStateOf(false) }

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

    // Load conversation if existing
    LaunchedEffect(conversationId) {
        if (conversationId != null) {
            val conv = prefs.getConversations().find { it.id == conversationId }
            if (conv != null) {
                messages.clear()
                messages.addAll(conv.messages)
                currentGuideStyle = conv.guideStyle
                activeModel = AvailableModels.models.find { it.guideStyleKey == conv.guideStyle } ?: activeModel
            }
        }
    }

    fun sendMessage(queryText: String) {
        val trimmed = queryText.trim()
        if (trimmed.isEmpty()) return

        // Check if free session expired
        if (!isPremium && freeSecondsLeft <= 0) {
            showPaywall = true
            return
        }

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
                // Ensure the message is never left empty under any circumstances
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
                // Save conversation
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

    val imeInsets = WindowInsets.ime
    val navBarInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    val bottomInsetPx = maxOf(imeInsets.getBottom(density), navBarInsets.getBottom(density))
    val bottomInsetDp = with(density) { bottomInsetPx.toDp() }

    LaunchedEffect(bottomInsetPx) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    DawnBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Redesigned ChatGPT / Claude Style Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button + Previous Chats button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showChatHistory = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .testTag("chat_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Previous Chats",
                            tint = Ink,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Center: ChatGPT/Claude Model Selector Pill
                ModelSelectorPill(
                    currentModel = activeModel,
                    onClick = { showModelSwitcher = true }
                )

                // Right: Free Tier Timer / Pro Badge + Voice Mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isPremium) {
                        val mins = freeSecondsLeft / 60
                        val secs = freeSecondsLeft % 60
                        val timeStr = String.format("%d:%02d", mins, secs)
                        val isUrgent = freeSecondsLeft <= 60

                        Surface(
                            onClick = { showPaywall = true },
                            shape = RoundedCornerShape(999.dp),
                            color = if (isUrgent) Color(0xFFFFECEB) else Color.White.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, if (isUrgent) Color(0xFFE53935) else Ember.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("free_timer_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isUrgent) Color(0xFFE53935) else Ember,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUrgent) Color(0xFFE53935) else EmberDeep,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Ember.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Ember.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "✦ Pro",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmberDeep,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToVoice,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .testTag("switch_to_voice_pill")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voice Mode",
                            tint = Ember,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Options menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.7f))
                                .testTag("chat_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Ink,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New chat") },
                            onClick = {
                                showMenu = false
                                messages.clear()
                                activeConversationId = UUID.randomUUID().toString()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Guide: ${currentGuideStyle.replaceFirstChar { it.uppercase() }}") },
                            onClick = {
                                showMenu = false
                                val styles = listOf("scholar", "shepherd", "storyteller", "friend")
                                val next = styles[(styles.indexOf(currentGuideStyle) + 1) % styles.size]
                                currentGuideStyle = next
                                prefs.guideStyle = next
                                Toast.makeText(context, "Guide changed to ${next.replaceFirstChar { it.uppercase() }}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear messages") },
                            onClick = {
                                showMenu = false
                                messages.clear()
                            }
                        )
                    }
                }
            }
        }

        // Message List or Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    // Empty state starter prompts
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SelahOrb(size = 80.dp, isPulsing = true)
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "How can I guide your reflection today?",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ask a question, explore a passage, or tap a topic below:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = InkSoft)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val starterPrompts = listOf(
                            "What does John 3:16 really mean?",
                            "How do I deal with anxiety biblically?",
                            "Who wrote the Psalms?",
                            "Explain grace like I'm new to this"
                        )

                        starterPrompts.forEach { prompt ->
                            Surface(
                                onClick = { sendMessage(prompt) },
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.75f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    ),
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(messages) { index, msg ->
                            // Optional timestamp between groups
                            if (index == 0 || (msg.timestamp - messages[index - 1].timestamp) > 300000) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.White.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp)),
                                            style = MaterialTheme.typography.labelSmall.copy(color = InkSoft),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            MessageBubble(
                                message = msg,
                                isStreamingLast = isStreaming && index == messages.size - 1 && msg.sender == "selah",
                                onReferenceClick = { ref ->
                                    val verse = VerseRepository.getByReference(ref)
                                    if (verse != null) {
                                        onOpenVerseDetail(verse.id)
                                    } else {
                                        sendMessage("Tell me about the passage $ref")
                                    }
                                },
                                onFollowUpClick = { q -> sendMessage(q) },
                                onLongPress = { selectedMessageForActions = msg }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            // Input Bar
            // Input Bar or Session Expired Paywall Bar
            if (!isPremium && freeSecondsLeft <= 0) {
                Surface(
                    onClick = { showPaywall = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = bottomInsetDp + 8.dp, top = 6.dp)
                        .testTag("chat_paywall_trigger_bar"),
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, Ember),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Ember.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Ember, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "Free 7-Min Reflection Ended",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Ink, fontSize = 14.sp)
                                )
                                Text(
                                    text = "Tap to unlock unlimited ($7.99/mo, $75/yr, $100 lifetime)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = InkSoft, fontSize = 11.sp)
                                )
                            }
                        }

                        Surface(shape = RoundedCornerShape(999.dp), color = Ember) {
                            Text(
                                text = "Upgrade",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = bottomInsetDp + 8.dp, top = 6.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
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
                                    text = "Ask Selah (${activeModel.name})…",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = InkSoft)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Ink,
                                unfocusedTextColor = Ink
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field")
                        )

                        // Mic / Stop / Send Buttons
                        if (isStreaming) {
                            IconButton(
                                onClick = {
                                    streamJob?.cancel()
                                    isStreaming = false
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Ember)
                                    .testTag("chat_stop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else if (inputQuery.isNotBlank()) {
                            IconButton(
                                onClick = { sendMessage(inputQuery) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Ember)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onNavigateToVoice,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Ember.copy(alpha = 0.12f))
                                    .testTag("chat_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Dictation",
                                    tint = Ember,
                                    modifier = Modifier.size(20.dp)
                                )
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
                containerColor = Color(0xFFFFF6F1),
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Ink)
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
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Ink)
                        Text("Copy message", style = MaterialTheme.typography.bodyLarge.copy(color = Ink))
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
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Ink)
                        Text("Read aloud", style = MaterialTheme.typography.bodyLarge.copy(color = Ink))
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
                        Icon(Icons.Default.Share, contentDescription = null, tint = Ink)
                        Text("Share reflection", style = MaterialTheme.typography.bodyLarge.copy(color = Ink))
                    }
                }
            }
        }

        // ChatGPT & Claude Style Model Switcher Sheet
        if (showModelSwitcher) {
            ModelSwitcherSheet(
                selectedModelId = activeModel.id,
                onModelSelected = { newModel ->
                    activeModel = newModel
                    prefs.activeModelId = newModel.id
                    currentGuideStyle = newModel.guideStyleKey
                },
                onDismiss = { showModelSwitcher = false }
            )
        }

        // Previous Chats Drawer / History Sheet
        if (showChatHistory) {
            ChatHistorySheet(
                prefs = prefs,
                activeConversationId = activeConversationId,
                onSelectConversation = { convId ->
                    activeConversationId = convId
                    val conv = prefs.getConversations().find { it.id == convId }
                    if (conv != null) {
                        messages.clear()
                        messages.addAll(conv.messages)
                        currentGuideStyle = conv.guideStyle
                        activeModel = AvailableModels.models.find { it.guideStyleKey == conv.guideStyle } ?: activeModel
                    }
                },
                onNewChat = {
                    streamJob?.cancel()
                    activeConversationId = UUID.randomUUID().toString()
                    messages.clear()
                    inputQuery = ""
                    isStreaming = false
                },
                onUpgradeClick = {
                    showPaywall = true
                },
                onDismiss = { showChatHistory = false }
            )
        }

        // Subscription Paywall Modal ($7.99/mo, $75/yr, $100 lifetime)
        if (showPaywall) {
            SubscriptionPaywallModal(
                prefs = prefs,
                onDismiss = { showPaywall = false },
                onSubscribed = {
                    isPremium = true
                    freeSecondsLeft = 9999
                    showPaywall = false
                    Toast.makeText(context, "Welcome to Selah Pro! Unlimited reflection unlocked.", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isStreamingLast: Boolean,
    onReferenceClick: (String) -> Unit,
    onFollowUpClick: (String) -> Unit,
    onLongPress: () -> Unit
) {
    val isUser = message.sender == "user"

    // If Selah is reflecting and hasn't started streaming tokens yet, render the single unified thinking widget
    if (!isUser && message.text.isBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 6.dp)
        ) {
            SelahOrb(size = 28.dp, isListening = true)
            Text(
                text = "Selah is reflecting…",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = InkSoft,
                    fontStyle = FontStyle.Italic
                )
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.widthIn(max = 340.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                SelahOrb(
                    size = 28.dp,
                    isPulsing = false,
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 22.dp,
                            topEnd = 22.dp,
                            bottomStart = if (isUser) 22.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 22.dp
                        )
                    )
                    .background(
                        if (isUser) {
                            Brush.linearGradient(listOf(Color(0xFFFF7A4D), Ember))
                        } else {
                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.8f)))
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isUser) Color.Transparent else Color.White,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable { onLongPress() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isUser) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            lineHeight = 22.sp
                        )
                    )
                } else {
                    FormattedSelahMessage(
                        text = message.text,
                        onReferenceClick = onReferenceClick
                    )
                }
            }
        }

        // Follow up chips
        if (!isUser && message.followups.isNotEmpty()) {
            FollowUpChips(
                followups = message.followups,
                onSelect = onFollowUpClick,
                modifier = Modifier.padding(start = 36.dp, top = 6.dp)
            )
        }
    }
}

@Composable
private fun FormattedSelahMessage(
    text: String,
    onReferenceClick: (String) -> Unit
) {
    val references = remember(text) { VerseRepository.findReferencesInText(text) }
    val lines = text.split("\n")

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
                            .background(Dawn100.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = Ember.copy(alpha = 0.2f),
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
                                    color = Ink
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
                            color = EmberDeep
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                else -> {
                    if (trimmed.isNotEmpty()) {
                        // Check if line contains a scripture reference chip
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
                                    color = Ink,
                                    lineHeight = 22.sp
                                ),
                                modifier = Modifier.clickable { onReferenceClick(matchedRef) }
                            )
                        } else {
                            Text(
                                text = trimmed,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Ink,
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
