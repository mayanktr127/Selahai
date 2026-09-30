package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.GeminiChatService
import com.example.data.ChatMessage
import com.example.data.SelahPreferences
import com.example.ui.components.DawnBackground
import com.example.ui.components.FollowUpParser
import com.example.ui.components.GlassCard
import com.example.ui.components.SelahOrb
import com.example.ui.theme.Ember
import com.example.ui.theme.EmberDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.InkSoft
import kotlinx.coroutines.launch
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    REFLECTING,
    SPEAKING
}

@Composable
fun VoiceReflectionScreen(
    prefs: SelahPreferences,
    onBack: () -> Unit,
    onNavigateToTextChat: (initialPrompt: String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    var userTranscript by remember { mutableStateOf("") }
    var aiSpokenReply by remember { mutableStateOf("") }
    var isMuted by remember { mutableStateOf(false) }
    var audioRms by remember { mutableFloatStateOf(0f) }

    val animatedRms by animateFloatAsState(
        targetValue = if (voiceState == VoiceState.LISTENING) (audioRms / 10f).coerceIn(0f, 0.25f) else 0f,
        label = "rms_glow"
    )

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

    // Speech Recognizer
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun speakAiReply(text: String) {
        if (isMuted) {
            voiceState = VoiceState.IDLE
            return
        }
        voiceState = VoiceState.SPEAKING
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "selah_voice_utt")
        // Check when TTS finishes
        scope.launch {
            kotlinx.coroutines.delay((text.split(" ").size * 320L).coerceAtLeast(3000L))
            if (voiceState == VoiceState.SPEAKING) {
                voiceState = VoiceState.IDLE
            }
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Voice recognition not available on device", Toast.LENGTH_SHORT).show()
            return
        }

        speechRecognizer?.destroy()
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                voiceState = VoiceState.LISTENING
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                audioRms = rmsdB
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                voiceState = VoiceState.REFLECTING
            }
            override fun onError(error: Int) {
                voiceState = VoiceState.IDLE
                Toast.makeText(context, "Listening timed out. Tap mic to talk again.", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""
                if (spokenText.isNotBlank()) {
                    userTranscript = spokenText
                    voiceState = VoiceState.REFLECTING

                    // Generate AI response
                    scope.launch {
                        val replyBuilder = StringBuilder()
                        try {
                            GeminiChatService.streamChat(
                                conversationHistory = listOf(ChatMessage(sender = "user", text = "Voice reflection: $spokenText")),
                                userName = prefs.userName,
                                guideStyle = prefs.guideStyle
                            ) { chunk ->
                                replyBuilder.append(chunk)
                                aiSpokenReply = FollowUpParser.parse(replyBuilder.toString()).first
                            }
                        } catch (e: Exception) {
                            aiSpokenReply = "Peace be with you. I am reflecting on your words: $spokenText."
                        } finally {
                            speakAiReply(aiSpokenReply)
                        }
                    }
                } else {
                    voiceState = VoiceState.IDLE
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull() ?: ""
                if (partial.isNotBlank()) {
                    userTranscript = partial
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
        voiceState = VoiceState.LISTENING
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        voiceState = VoiceState.IDLE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(context, "Audio permission needed for voice reflection", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            tts?.stop()
        }
    }

    DawnBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Back, "Voice Chat" pill, and chat switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.65f))
                        .testTag("voice_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Ember)
                        )
                        Text(
                            text = "Voice Chat",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { onNavigateToTextChat(userTranscript.ifBlank { null }) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.65f))
                        .testTag("switch_to_text_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Text Chat",
                        tint = Ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Piece: Large Glowing Selah Orb with rippling halo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SelahOrb(
                    size = 190.dp,
                    isListening = voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING,
                    isPulsing = true,
                    haloScaleExtra = animatedRms
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Status text
                val statusText = when (voiceState) {
                    VoiceState.LISTENING -> "Selah is listening…"
                    VoiceState.REFLECTING -> "Selah is reflecting…"
                    VoiceState.SPEAKING -> "Selah is speaking…"
                    VoiceState.IDLE -> "Tap the microphone to speak"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (voiceState == VoiceState.LISTENING) Ember else Ink
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Captions / Transcript Card
                if (userTranscript.isNotBlank() || aiSpokenReply.isNotBlank()) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (userTranscript.isNotBlank()) {
                                Text(
                                    text = "\"$userTranscript\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = InkSoft,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (aiSpokenReply.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = aiSpokenReply,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Ink,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 22.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Controls: Speaker Mute | Big Mic Button | Guide style toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker Mute/Unmute
                IconButton(
                    onClick = {
                        isMuted = !isMuted
                        if (isMuted) tts?.stop()
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speaker Toggle",
                        tint = if (isMuted) InkSoft else Ember
                    )
                }

                // Center Big Circular Mic with Ember Glow
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = Ember, spotColor = EmberDeep)
                        .clip(CircleShape)
                        .background(if (voiceState == VoiceState.LISTENING) Ember else Color.White)
                        .clickable {
                            if (voiceState == VoiceState.LISTENING) {
                                stopListening()
                            } else {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("voice_center_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (voiceState == VoiceState.LISTENING) Color.White else Ember,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Guide style switcher shortcut
                IconButton(
                    onClick = {
                        val styles = listOf("scholar", "shepherd", "storyteller", "friend")
                        val next = styles[(styles.indexOf(prefs.guideStyle) + 1) % styles.size]
                        prefs.guideStyle = next
                        Toast.makeText(context, "Voice Guide: ${next.replaceFirstChar { it.uppercase() }}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Switch Guide",
                        tint = Ink
                    )
                }
            }
        }
    }
}
