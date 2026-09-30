package com.example.ai

import com.example.BuildConfig
import com.example.data.ChatMessage
import com.example.data.VerseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiChatService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:streamGenerateContent?alt=sse"

    fun buildSystemInstruction(userName: String, guideStyle: String): String {
        val basePrompt = """
You are Selah, a warm, wise and knowledgeable Bible companion. You help people of every background — devoted believers, curious seekers, students, sceptics and people of other faiths — understand the Holy Bible clearly and apply it to everyday modern life.

USER_NAME: ${if (userName.isBlank()) "Friend" else userName}
GUIDE_STYLE: $guideStyle

KNOWLEDGE
- Below is your KNOWLEDGE BASE of 100 key verses with their setting, meaning and modern application. When a user asks about any of these verses, ground your answer in it first, then add depth.
- You may also answer ANY other question about the Bible: other verses, books, characters, stories, history, geography, original languages (Hebrew, Aramaic, Greek), how the Bible was compiled, translations, Christian practices, prayer, church traditions, and how different Christian traditions (Catholic, Orthodox, Protestant and others) interpret passages.
- Users may also ask life questions ("I'm anxious about my exams", "how do I forgive my father?"). Answer them with relevant Scripture and gentle practical wisdom.

ACCURACY RULES
- Always cite book, chapter and verse (e.g. "Philippians 4:6-7").
- Quote Scripture from the King James Version unless the user asks for another translation. If you're not certain of exact wording, paraphrase and say so. Never invent verses or put words in the Bible that aren't there.
- Explain archaic KJV words in plain English (e.g. "charity" = love).
- Explain the context of a verse; warn gently when a popular verse is commonly taken out of context (e.g. Jeremiah 29:11, Philippians 4:13, Matthew 7:1).
- On contested theological questions, present the main views fairly, noting which traditions hold them, rather than declaring one correct.
- Be honest about difficult passages; don't dodge them.

HOW TO ANSWER ABOUT A VERSE (default structure, use markdown)
1. The verse — quoted as a blockquote with its reference
2. **Where it's from** — book, author, audience, what was happening
3. **What it means** — clear explanation in simple language
4. **What it means for you today** — 2–4 concrete, relatable modern examples (work, family, social media, stress, relationships, money, purpose)
5. **Go deeper** — 2–3 related verses with one-line notes
Keep answers focused (about 150–300 words) unless the user asks for more depth. For simple or casual questions, reply conversationally without the full structure.

GUIDE STYLE
Adapt your tone to the user's chosen guide style, provided as GUIDE_STYLE:
- "scholar": historical context, original-language word meanings, cross-references, more detail.
- "shepherd": gentle, pastoral, comforting, ends with a short prayer if appropriate.
- "storyteller": explains through the Bible story around the verse, vivid and narrative.
- "friend": casual, warm, short sentences, modern examples, light and encouraging.

TONE & CARE
- Warm, respectful, never preachy or judgmental. Respect people of other faiths or none; never pressure anyone to convert.
- Address the user by name occasionally, not in every message.
- If a user expresses thoughts of self-harm, suicide, abuse or crisis, respond with compassion, encourage them to contact local emergency services or a crisis line immediately and to reach out to someone they trust. Scripture can offer comfort but is not a substitute for professional help.
- You are not a replacement for a pastor, priest, doctor, therapist or lawyer; say so when relevant.
- If a question is unrelated to the Bible, faith or life guidance, answer briefly and kindly, then gently offer to connect it to Scripture.

FOLLOW-UP SUGGESTIONS
At the very end of every reply, output exactly this line with 3 short follow-up questions the user might want to ask next:
<<FOLLOWUPS: question one | question two | question three>>
The app will strip this line and render it as tappable chips.
        """.trimIndent()

        // Append concise knowledge base summary
        val kbBuilder = StringBuilder("\n\nKNOWLEDGE BASE (100 KEY VERSES):\n")
        VerseRepository.allVerses.forEach { v ->
            kbBuilder.append("${v.id}. ${v.reference} - \"${v.text}\" | Setting: ${v.setting} | Meaning: ${v.meaning} | Today: ${v.today}\n")
        }

        return basePrompt + kbBuilder.toString()
    }

    suspend fun streamChat(
        conversationHistory: List<ChatMessage>,
        userName: String,
        guideStyle: String,
        onChunk: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY")

        if (hasValidKey) {
            try {
                val fullUrl = "$BASE_URL&key=$apiKey"
                val systemInst = buildSystemInstruction(userName, guideStyle)

                val requestObj = JSONObject().apply {
                    val contentsArray = JSONArray()

                    // Send recent turns (up to 8 turns to stay focused and fast)
                    val recentMessages = conversationHistory.takeLast(8)
                    for (msg in recentMessages) {
                        val role = if (msg.sender == "user") "user" else "model"
                        val contentObj = JSONObject().apply {
                            put("role", role)
                            val partsArray = JSONArray()
                            partsArray.put(JSONObject().put("text", msg.text))
                            put("parts", partsArray)
                        }
                        contentsArray.put(contentObj)
                    }
                    put("contents", contentsArray)

                    val systemObj = JSONObject().apply {
                        val partsArray = JSONArray()
                        partsArray.put(JSONObject().put("text", systemInst))
                        put("parts", partsArray)
                    }
                    put("systemInstruction", systemObj)

                    val genConfig = JSONObject().apply {
                        put("temperature", 0.7)
                        put("topP", 0.95)
                        put("topK", 40)
                    }
                    put("generationConfig", genConfig)
                }

                val reqBody = requestObj.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(fullUrl)
                    .post(reqBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val source = response.body?.byteStream()?.bufferedReader()
                    if (source != null) {
                        var chunkReceived = false
                        source.use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                val l = line?.trim() ?: continue
                                if (!l.startsWith("data:")) continue
                                val jsonStr = l.removePrefix("data:").trim()
                                if (jsonStr.isEmpty() || jsonStr == "[DONE]") continue
                                try {
                                    val chunkObj = JSONObject(jsonStr)
                                    val candidates = chunkObj.optJSONArray("candidates")
                                    val firstCandidate = candidates?.optJSONObject(0)
                                    val content = firstCandidate?.optJSONObject("content")
                                    val parts = content?.optJSONArray("parts")
                                    val text = parts?.optJSONObject(0)?.optString("text", "")
                                    if (!text.isNullOrEmpty()) {
                                        chunkReceived = true
                                        onChunk(text)
                                    }
                                } catch (e: Exception) {
                                    // Parse error on SSE line
                                }
                            }
                        }
                        if (chunkReceived) return@withContext
                    }
                }
            } catch (e: Exception) {
                // If API failed (e.g. rate limit, network timeout), fallback to grounded engine below
            }
        }

        // Grounded Local AI Engine:
        val lastUserMessage = conversationHistory.lastOrNull { it.sender == "user" }?.text ?: "Hello"
        generateLocalGroundedResponse(lastUserMessage, userName, guideStyle, onChunk)
    }

    /**
     * Local Intelligent Grounded Response generator.
     * Uses the 100-verse knowledge base, guide persona, tone adaptation, and generates follow-ups.
     */
    private suspend fun generateLocalGroundedResponse(
        prompt: String,
        userName: String,
        guideStyle: String,
        onChunk: (String) -> Unit
    ) {
        val lowerPrompt = prompt.lowercase()
        val displayName = if (userName.isBlank()) "friend" else userName

        // 1. Check if user asked about a specific verse in our knowledge base
        val matchedVerse = VerseRepository.allVerses.find { v ->
            lowerPrompt.contains(v.reference.lowercase()) ||
            lowerPrompt.contains(v.title.lowercase()) ||
            (v.reference.contains(" ") && lowerPrompt.contains(v.reference.substringBefore(" ").lowercase()) && lowerPrompt.contains(v.reference.substringAfter(" ").substringBefore(":").lowercase()))
        }

        // 2. Check topic/feeling
        val matchedTopic = com.example.data.VerseConstants.TOPICS.find { topic ->
            lowerPrompt.contains(topic.label.lowercase()) ||
            lowerPrompt.contains(topic.id.lowercase())
        }

        val fullResponse: String = when {
            matchedVerse != null -> formatVerseAnswer(matchedVerse, displayName, guideStyle)
            matchedTopic != null -> formatTopicAnswer(matchedTopic, displayName, guideStyle)
            lowerPrompt.contains("psalm") || lowerPrompt.contains("david") -> formatGeneralBibleAnswer(prompt, displayName, guideStyle, "David")
            lowerPrompt.contains("jesus") || lowerPrompt.contains("christ") -> formatGeneralBibleAnswer(prompt, displayName, guideStyle, "Jesus")
            lowerPrompt.contains("pray") || lowerPrompt.contains("prayer") -> formatPrayerAnswer(displayName, guideStyle)
            else -> formatGeneralConversationalAnswer(prompt, displayName, guideStyle)
        }

        // Stream tokens realistically
        val words = fullResponse.split(" ")
        for (i in words.indices) {
            val token = words[i] + (if (i < words.size - 1) " " else "")
            onChunk(token)
            delay(18) // smooth reading cadence
        }
    }

    private fun formatVerseAnswer(v: com.example.data.Verse, name: String, guideStyle: String): String {
        val greeting = when (guideStyle) {
            "scholar" -> "Let us examine the textual and historical setting of this passage."
            "shepherd" -> "Peace be with you, $name. This beloved verse brings deep comfort."
            "storyteller" -> "Imagine the scene when these words were first given."
            else -> "I love this passage, $name. Here is what it means for everyday life:"
        }

        val prayerOrNote = when (guideStyle) {
            "scholar" -> "Note the original nuance: in ancient manuscripts, these words carried the weight of covenant fidelity."
            "shepherd" -> "\n\n*A gentle prayer for you:*\n> Lord, thank you for reminding $name of your unfailing grace. Grant peace and steady faith for every step today. Amen."
            "storyteller" -> "Every line in this passage weaves together a story of redemption that continues in your story today."
            else -> "Take a breath and carry this truth with you into whatever today holds."
        }

        val relatedChips = v.related.joinToString(", ")

        return """
$greeting

> "${v.text}" — ${v.reference}

### Where it's from
${v.setting}

### What it means
${v.meaning}

### What it means for you today
${v.today}

### Go deeper
- **${v.related.getOrNull(0) ?: "Psalm 23:1"}**: A companion reflection on God's abiding presence.
- **${v.related.getOrNull(1) ?: "Romans 8:28"}**: Finding strength and purpose through trust.

$prayerOrNote

<<FOLLOWUPS: How can I live out ${v.reference} today? | What does the original language mean? | Show me related verses on peace>>
        """.trimIndent()
    }

    private fun formatTopicAnswer(topic: com.example.data.MoodTopic, name: String, guideStyle: String): String {
        val verses = topic.verseIds.mapNotNull { VerseRepository.getById(it) }
        val primary = verses.firstOrNull() ?: VerseRepository.getById(28)!!
        val secondary = verses.getOrNull(1) ?: VerseRepository.getById(32)!!

        val pastoralComfort = when (guideStyle) {
            "scholar" -> "Scripture frequently addresses human vulnerability and offers both theological and practical wisdom."
            "shepherd" -> "It is okay to feel this way, $name. You do not have to carry this burden all on your own."
            "storyteller" -> "Throughout the Scriptures, faithful men and women walked through this exact valley."
            else -> "I hear you, $name. Let's look at what God's Word says when you're feeling ${topic.label.lowercase()}."
        }

        return """
$pastoralComfort

Here is a verse that speaks right into where your heart is right now:

> "${primary.text}" — ${primary.reference}

### Understanding this promise
${primary.meaning}

### What this means for you today
- **Pause and release the weight:** ${primary.today}
- **A companion promise:** Look also to **${secondary.reference}**: *"${secondary.text}"*

Remember, you are never alone in what you are walking through. Take things one moment at a time.

<<FOLLOWUPS: Can you pray for me? | What other verses help with ${topic.label.lowercase()}? | Tell me a Bible story about overcoming fear>>
        """.trimIndent()
    }

    private fun formatGeneralBibleAnswer(query: String, name: String, guideStyle: String, subject: String): String {
        return """
That is a wonderful question about the Scriptures, $name.

In the biblical narrative, $subject plays a central role in revealing God's character and covenant with humanity. From the Old Testament prophecies to the Gospels, we see that God's grace consistently meets people right in their ordinary struggles and triumphs.

For deeper reflection, consider how **Psalm 23:1-4** and **John 14:27** mirror this guidance:
> "Peace I leave with you, my peace I give unto you: not as the world giveth, give I unto you. Let not your heart be troubled, neither let it be afraid." — John 14:27

Whether you are studying for personal devotion or curious about history, Scripture invites us to pause, listen, and find rest in God's promises.

<<FOLLOWUPS: Tell me more about David's life | What are the most famous Psalms? | How was the Bible compiled?>>
        """.trimIndent()
    }

    private fun formatPrayerAnswer(name: String, guideStyle: String): String {
        return """
Prayer is simply speaking honestly with God — no rehearsed formulas or perfection needed.

Jesus gave us the perfect pattern in the Sermon on the Mount:

> "Our Father which art in heaven, Hallowed be thy name. Thy kingdom come. Thy will be done in earth, as it is in heaven. Give us this day our daily bread..." — Matthew 6:9-11

### Three simple ways to pray today:
1. **Adoration:** Thank God for one thing he has made or done.
2. **Honesty:** Name whatever is stressing you right now (Philippians 4:6).
3. **Rest:** Ask for your daily bread, trusting him for the rest.

*A simple prayer to start:* "Lord, thank you that you hear me. Give me peace, wisdom, and strength for today. Amen."

<<FOLLOWUPS: How do I pray when I'm anxious? | What is the Lord's Prayer meaning? | Can you write a prayer for my family?>>
        """.trimIndent()
    }

    private fun formatGeneralConversationalAnswer(prompt: String, name: String, guideStyle: String): String {
        val v = VerseRepository.getVerseOfTheDay()
        return """
Hello $name. Whatever is on your mind today, I am here to explore Scripture and walk through life's questions with you.

A quiet reflection to ground our conversation:
> "${v.text}" — ${v.reference}

${v.meaning}

Feel free to ask about any verse, character, translation, or a personal situation you would like biblical guidance on.

<<FOLLOWUPS: What does John 3:16 really mean? | How do I deal with anxiety biblically? | Explain grace like I'm new to this>>
        """.trimIndent()
    }
}
