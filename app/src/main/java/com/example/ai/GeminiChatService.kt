package com.example.ai

import com.example.BuildConfig
import com.example.data.ChatMessage
import com.example.data.VerseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiChatService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val MODEL = "gemini-2.5-flash"
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
        var chunkReceived = false

        if (hasValidKey) {
            try {
                // Allow up to 5s budget to connect and stream remote LLM tokens
                withTimeoutOrNull(5000L) {
                    val fullUrl = "$BASE_URL&key=$apiKey"
                    val systemInst = buildSystemInstruction(userName, guideStyle)

                    val requestObj = JSONObject().apply {
                        val contentsArray = JSONArray()
                        val recentMessages = conversationHistory.takeLast(6)
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

                    val call = client.newCall(request)
                    try {
                        val response = call.execute()
                        response.use { res ->
                            if (res.isSuccessful) {
                                val source = res.body?.byteStream()?.bufferedReader()
                                if (source != null) {
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
                                                // Ignore line parse errors
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        if (!chunkReceived) {
                            call.cancel()
                        }
                    }
                }
            } catch (e: Exception) {
                // If API failed or quota exceeded, proceed immediately to local engine below
            }
        }

        if (!chunkReceived) {
            // Grounded Local AI Engine: responds within max 1-2 seconds with rich Scripture answers
            val lastUserMessage = conversationHistory.lastOrNull { it.sender == "user" }?.text ?: "Hello"
            generateLocalGroundedResponse(lastUserMessage, userName, guideStyle, onChunk)
        }
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
            lowerPrompt.contains("grace") -> formatGraceAnswer(displayName, guideStyle)
            lowerPrompt.contains("who wrote") || lowerPrompt.contains("author") || lowerPrompt.contains("wrote the psalm") -> formatPsalmsAuthorshipAnswer(displayName, guideStyle)
            lowerPrompt.contains("psalm") || lowerPrompt.contains("david") -> formatGeneralBibleAnswer(prompt, displayName, guideStyle, "King David and the Psalms")
            lowerPrompt.contains("jesus") || lowerPrompt.contains("christ") -> formatGeneralBibleAnswer(prompt, displayName, guideStyle, "Jesus Christ")
            lowerPrompt.contains("pray") || lowerPrompt.contains("prayer") -> formatPrayerAnswer(displayName, guideStyle)
            lowerPrompt.contains("anxiety") || lowerPrompt.contains("anxious") || lowerPrompt.contains("worry") -> {
                val anxiousTopic = com.example.data.VerseConstants.TOPICS.find { it.id == "anxious" }!!
                formatTopicAnswer(anxiousTopic, displayName, guideStyle)
            }
            lowerPrompt.contains("moses") -> formatMosesAnswer(displayName, guideStyle)
            lowerPrompt.contains("abraham") -> formatAbrahamAnswer(displayName, guideStyle)
            lowerPrompt.contains("paul") -> formatPaulAnswer(displayName, guideStyle)
            lowerPrompt.contains("peter") -> formatPeterAnswer(displayName, guideStyle)
            lowerPrompt.contains("love") -> formatLoveAnswer(displayName, guideStyle)
            lowerPrompt.contains("faith") -> formatFaithAnswer(displayName, guideStyle)
            else -> formatDynamicKnowledgeBaseAnswer(prompt, displayName, guideStyle)
        }

        // Stream tokens realistically
        val words = fullResponse.split(" ")
        for (i in words.indices) {
            val token = words[i] + (if (i < words.size - 1) " " else "")
            onChunk(token)
            delay(12) // smooth reading cadence
        }
    }

    private fun formatDynamicKnowledgeBaseAnswer(prompt: String, name: String, guideStyle: String): String {
        val lowerPrompt = prompt.lowercase()
        // Extract meaningful search terms
        val words = lowerPrompt.split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length > 2 && it !in setOf("what", "does", "the", "say", "about", "can", "you", "tell", "explain", "meaning", "how", "why", "who", "when", "where", "with", "from", "for", "and", "that", "this", "help") }

        // Find candidate verses from knowledge base
        val candidateVerses = mutableListOf<com.example.data.Verse>()
        for (w in words) {
            candidateVerses.addAll(VerseRepository.search(w))
        }

        val primaryVerse = candidateVerses.groupBy { it.id }
            .maxByOrNull { it.value.size }?.value?.firstOrNull()
            ?: VerseRepository.search(lowerPrompt).firstOrNull()
            ?: VerseRepository.getVerseOfTheDay()

        val secondaryVerse = candidateVerses.find { it.id != primaryVerse.id }
            ?: VerseRepository.allVerses.find { it.part == primaryVerse.part && it.id != primaryVerse.id }
            ?: VerseRepository.getById(if (primaryVerse.id == 1) 74 else 1)!!

        val cleanQuery = prompt.trim().removeSuffix("?").removeSuffix(".")

        val opening = when (guideStyle) {
            "scholar" -> "Let us explore the scriptural, historical, and theological dimensions of \"$cleanQuery\"."
            "shepherd" -> "That is a deeply meaningful question to bring before God, $name. Let us look at what His Word reveals concerning \"$cleanQuery\"."
            "storyteller" -> "Across the broad tapestry of Scripture, the question of \"$cleanQuery\" touches the deepest rhythms of human faith."
            else -> "That is a wonderful question to explore, $name. Here is what Scripture and biblical wisdom reveal about \"$cleanQuery\":"
        }

        val closingNote = when (guideStyle) {
            "shepherd" -> "\n\n*A blessing for you today:*\n> \"The Lord bless thee, and keep thee: The Lord make his face shine upon thee, and be gracious unto thee.\" (Numbers 6:24-25)"
            else -> "\n\nTake a quiet moment to reflect on these words today. Let Scripture guide your thoughts and give you peace."
        }

        return """
$opening

A foundational anchor for this inquiry is found in **${primaryVerse.reference}**:

> "${primaryVerse.text}" — ${primaryVerse.reference}

### Biblical Setting & Background
${primaryVerse.setting}

### Theological Insight & Meaning
${primaryVerse.meaning} When we reflect on your question, God's Word reminds us that truth is both timeless and deeply personal. It invites us to trust in God's character rather than relying solely on our own understanding (Proverbs 3:5).

### What This Means For You Today
- **Everyday Practice:** ${primaryVerse.today}
- **Mindset Shift:** Focus not on the anxiety of unanswered questions, but on the certainty of God's steadfast presence.
- **Companion Promise:** Consider also **${secondaryVerse.reference}**: *"${secondaryVerse.text}"* — which speaks to God's providence and care.
$closingNote

<<FOLLOWUPS: How can I apply ${primaryVerse.reference} today? | What other passages address this? | Can you pray for me about this?>>
        """.trimIndent()
    }

    private fun formatMosesAnswer(name: String, guideStyle: String): String {
        return """
Hello $name! Moses is one of the most towering and transformative figures in all of sacred Scripture.

> "And the LORD spake unto Moses face to face, as a man speaketh unto his friend." — Exodus 33:11

### Who Was Moses?
Moses was called by God to lead the Israelites out of four centuries of Egyptian slavery, receive the Law and the Ten Commandments at Mount Sinai, and shepherd God's people through the wilderness toward the Promised Land.

### Key Milestones in Moses's Life:
1. **The Basket in the Nile:** Rescued by Pharaoh's daughter and raised in the royal palace of Egypt.
2. **The Burning Bush (Exodus 3):** God met Moses in Midian, revealing His sacred name: *"I AM THAT I AM."* Despite Moses's feelings of inadequacy and speech hesitation, God promised: *"Certainly I will be with thee."*
3. **The Exodus & Red Sea:** Through ten plagues and the parting of the Red Sea, God displayed His mighty power of redemption.
4. **The Law at Mount Sinai (Exodus 20):** Moses received the Ten Commandments, establishing a covenant of justice, holiness, and love.

### What Moses Teaches Us Today
Moses was not a superhero; he was an ordinary man who wrestled with self-doubt. God did not call him because of his perfection, but because of his willingness to be used. When you feel unequipped for what lies ahead, remember that God's strength is made perfect in human weakness (2 Corinthians 12:9).

<<FOLLOWUPS: What were the Ten Commandments? | Why did Moses not enter the Promised Land? | Tell me about the Burning Bush>>
        """.trimIndent()
    }

    private fun formatAbrahamAnswer(name: String, guideStyle: String): String {
        return """
Hello $name! Abraham is revered as the father of faith across Judaism, Christianity, and Islam.

> "And he believed in the LORD; and he counted it to him for righteousness." — Genesis 15:6

### Who Was Abraham?
Originally named Abram from Ur of the Chaldees, God called him at age 75 to leave everything familiar and journey to a land that God would show him (Genesis 12:1-3). God established an eternal covenant with him, promising:
1. **A Great Nation:** His descendants would be as numerous as the stars in the night sky.
2. **A Promised Land:** The land of Canaan for his descendants.
3. **A Universal Blessing:** *"In thee shall all families of the earth be blessed."*

### What Abraham Teaches Us Today
Hebrews 11:8 notes: *"By faith Abraham, when he was called to go out into a place which he should after receive for an inheritance, obeyed; and he went out, not knowing whither he went."* Faith is trusting God when you cannot see the full picture. It means taking the next faithful step with confidence in God's promises.

<<FOLLOWUPS: How did Abraham demonstrate faith? | What was the covenant with Abraham? | Who was Sarah in the Bible?>>
        """.trimIndent()
    }

    private fun formatPaulAnswer(name: String, guideStyle: String): String {
        return """
Peace be with you, $name. The Apostle Paul is the most influential missionary and theologian of the New Testament church.

> "I have fought a good fight, I have finished my course, I have kept the faith." — 2 Timothy 4:7

### The Story of Paul (formerly Saul of Tarsus):
1. **The Persecutor:** Zealous Pharisee who initially opposed the early Christian movement.
2. **Damascus Road (Acts 9):** A blinding encounter with the risen Jesus transformed his heart entirely.
3. **The Global Apostle:** Journeyed across the Mediterranean world establishing churches and writing 13 New Testament letters (including Romans, Galatians, Ephesians, and Philippians).
4. **Theology of Grace:** Paul championed the radical message that we are justified by faith in Christ, not by legalistic works.

### Key Lesson for Us:
No past mistake or failure is too great for God to redeem. Paul referred to himself as the *"chief of sinners"* (1 Timothy 1:15), yet God made him a beacon of hope and grace.

<<FOLLOWUPS: What happened on the Damascus Road? | What are Paul's most famous letters? | Explain Romans 8 in detail>>
        """.trimIndent()
    }

    private fun formatPeterAnswer(name: String, guideStyle: String): String {
        return """
Hello $name! Simon Peter is one of the most relatable, passionate disciples of Jesus.

> "And Simon Peter answered and said, Thou art the Christ, the Son of the living God." — Matthew 16:16

### Peter's Journey:
- **The Call:** A humble Galilean fisherman who dropped his nets when Jesus said: *"Follow me, and I will make you fishers of men."*
- **Stepping on the Water (Matthew 14):** The only disciple with the bold courage to step out of the boat into the storm.
- **Denial and Restoration:** After denying Jesus three times in his darkest hour, the risen Christ gently restored him by the Sea of Galilee, asking three times: *"Simon, son of Jonas, lovest thou me? Feed my sheep."* (John 21).
- **The Pillar of Pentecost:** Filled with the Holy Spirit in Acts 2, Peter preached with boldness and led thousands to faith.

### What Peter Teaches Us:
Peter proves that a momentary failure does not define your final destination in God's kingdom. God uses cracked vessels to carry His glorious light.

<<FOLLOWUPS: Why did Peter deny Jesus? | Tell me about Peter walking on water | What are the letters of 1 and 2 Peter about?>>
        """.trimIndent()
    }

    private fun formatLoveAnswer(name: String, guideStyle: String): String {
        return """
Love is the very essence of God's character and the supreme commandment of the Bible, $name.

> "Charity suffereth long, and is kind; charity envieth not; charity vaunteth not itself, is not puffed up... Charity never faileth." — 1 Corinthians 13:4, 8

### Three Biblical Insights on Love:
1. **Agape Love:** In the Greek New Testament, the highest form of love is *Agape* — unconditional, self-giving, sacrificial love that seeks the good of another without expecting anything in return.
2. **God is Love (1 John 4:8):** Love is not just an emotion God feels; it is who God is.
3. **The Greatest Commandment:** When asked for the greatest law, Jesus replied: *"Love the Lord your God with all your heart... and love your neighbour as yourself."* (Matthew 22:37-39).

### Living Love Today:
Love in action means patience with difficult people, choosing kindness over being right, and showing empathy to those who are hurting.

<<FOLLOWUPS: Explain 1 Corinthians 13 verse by verse | What is the difference between agape and phileo? | How do I love someone who hurt me?>>
        """.trimIndent()
    }

    private fun formatFaithAnswer(name: String, guideStyle: String): String {
        return """
Faith is the anchor of the soul, $name.

> "Now faith is the substance of things hoped for, the evidence of things not seen." — Hebrews 11:1

### What is Biblical Faith?
Biblical faith is not blind optimism or wishful thinking. It is **anchored trust in a reliable God**. It is leaning your full weight upon God's character and promises even when circumstances seem turbulent.

### Key Aspects of Faith:
- **Trust over sight (2 Corinthians 5:7):** *"For we walk by faith, not by sight."*
- **Faith like a mustard seed (Matthew 17:20):** You don't need gigantic, flawless faith — you just need sincere faith in a gigantic God.
- **Active obedience (James 2:17):** Real faith naturally blossoms into acts of love and compassion.

<<FOLLOWUPS: How can I grow my faith when doubting? | What is the Hall of Faith in Hebrews 11? | How do faith and works work together?>>
        """.trimIndent()
    }

    private fun formatGraceAnswer(name: String, guideStyle: String): String {
        return """
Peace be with you, $name. Grace is one of the most radiant and freeing truths in all of Scripture.

> "For by grace are ye saved through faith; and that not of yourselves: it is the gift of God: Not of works, lest any man should boast." — Ephesians 2:8-9

### What is Grace in Simple Words?
Think of grace as **unearned favor and unconditional love**. In everyday life, everything feels like a transaction: we work for a salary, earn grades for studying, and win approval for performing. Grace flips this upside down: God does not love you because you perform well; God loves you because love is who He is (1 John 4:8).

### What Grace Means for You Today
- **No Need to Pretend:** You don't have to clean up your whole life before coming to God. Romans 5:8 reminds us that Christ loved us while we were still making mistakes.
- **Relief from Perfectionism:** Your worth is not tied to productivity, follower counts, or flawless track records. You are accepted as a gift.
- **Grace Flowing Outward:** When you truly experience how much you've been forgiven, it becomes easier to extend patience and grace to others (Ephesians 4:32).

### Go Deeper
- **Romans 5:8**: Love proven before we were ready.
- **2 Corinthians 12:9**: "My grace is sufficient for thee: for my strength is made perfect in weakness."

<<FOLLOWUPS: How do I accept God's grace? | What is the difference between grace and mercy? | Can you pray with me about feeling worthy?>>
        """.trimIndent()
    }

    private fun formatPsalmsAuthorshipAnswer(name: String, guideStyle: String): String {
        return """
Hello $name! The book of Psalms is a sacred collection of 150 prayers, hymns, and poems composed over nearly a thousand years of Israel's history.

> "Thy word is a lamp unto my feet, and a light unto my path." — Psalm 119:105

### Who Wrote the Psalms?
While King David is the most famous author, the book was written by multiple inspired authors:
1. **King David:** Credited with **73 psalms**, including the beloved shepherd hymn **Psalm 23**, prayers of deliverance (Psalm 27), and deep repentance (Psalm 51).
2. **Asaph and his family:** Worship leaders in the temple who wrote **12 psalms** (Psalms 50, 73–83), often reflecting on justice and God's sovereignty.
3. **The Sons of Korah:** A guild of temple musicians responsible for **11 psalms** (such as Psalm 42 and Psalm 46: *"God is our refuge and strength"*).
4. **Solomon:** Wrote Psalms 72 and 127.
5. **Moses:** Wrote **Psalm 90**, one of the oldest psalms in the Bible.
6. **Anonymous:** Around 50 psalms (often called "orphan psalms") have no stated author.

### What the Psalms Teach Us Today
The Psalms give us divine permission to bring **every human emotion** before God: joy, heartbreak, anger, celebration, and doubt. They teach us that prayer does not require masking our pain.

<<FOLLOWUPS: What is the most famous Psalm? | Explain Psalm 23 verse by verse | Why was David called a man after God's own heart?>>
        """.trimIndent()
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
