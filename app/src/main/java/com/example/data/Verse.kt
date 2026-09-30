package com.example.data

data class Verse(
    val id: Int,              // 1–100
    val reference: String,       // "John 3:16"
    val title: String,           // "God So Loved the World"
    val text: String,            // KJV verse text
    val part: String,            // "God's Love", "Peace, Worry & Anxiety", etc.
    val setting: String,
    val meaning: String,
    val today: String,
    val themes: List<String>,
    val related: List<String>
) {
    val bookAbbreviation: String
        get() {
            val bookPart = reference.substringBefore(" ").trim()
            val clean = bookPart.replace(Regex("[0-9]"), "").trim()
            return when {
                reference.startsWith("1 ") || reference.startsWith("2 ") || reference.startsWith("3 ") -> {
                    val num = reference.take(1)
                    val rest = reference.drop(2).substringBefore(" ")
                    (num + rest.take(2)).uppercase()
                }
                clean.length >= 3 -> clean.take(3).uppercase()
                else -> clean.uppercase()
            }
        }
}

data class MoodTopic(
    val id: String,
    val label: String,
    val prompt: String,
    val verseIds: List<Int>
)

object VerseConstants {
    val TOPICS = listOf(
        MoodTopic("anxious", "Anxious", "I'm feeling anxious and overwhelmed. What does the Bible say to bring me peace?", listOf(28, 29, 30, 31, 32, 33, 34, 35, 36)),
        MoodTopic("afraid", "Afraid", "I am feeling afraid right now. What Scripture reminds me of God's protection and courage?", listOf(5, 38, 39, 41, 42, 44, 45)),
        MoodTopic("grieving", "Grieving", "My heart is grieving and broken. How does God comfort those in sorrow?", listOf(50, 51, 53, 55, 57, 79)),
        MoodTopic("lonely", "Lonely", "I feel so lonely and unseen today. What verses remind me that God is with me?", listOf(3, 42, 58, 16)),
        MoodTopic("weary", "Weary", "I feel burnt out, exhausted, and weary. How can I find true rest in God?", listOf(31, 40, 43, 66)),
        MoodTopic("guilty", "Guilty", "I am struggling with guilt and regret over my mistakes. What does the Bible say about forgiveness and grace?", listOf(2, 59, 67, 70, 71, 73)),
        MoodTopic("hurt", "Hurt", "Someone hurt and betrayed me, and I feel angry. How can I navigate forgiveness and healing?", listOf(13, 14, 27, 73, 91, 92)),
        MoodTopic("lost", "Lost", "I feel lost and need wisdom for my direction in life. What guidance does Scripture offer?", listOf(18, 22, 23, 90, 94, 78)),
        MoodTopic("low_worth", "Low worth", "I'm struggling with low self-worth and comparison. How does God see my value?", listOf(6, 82, 87, 59)),
        MoodTopic("grateful", "Grateful", "My heart is full of gratitude and joy today! What verses can help me praise God?", listOf(47, 64, 97, 100)),
        MoodTopic("tempted", "Tempted", "I am facing temptation and struggling with a habit. How can I find strength and a way out?", listOf(46, 48, 96)),
        MoodTopic("starting_new", "Starting new", "I am starting a new chapter in my life. What promises can I hold onto for new beginnings?", listOf(39, 59, 62, 23, 85))
    )

    val PARTS = listOf(
        "God's Love",
        "Loving Others",
        "Faith & Trust",
        "Peace, Worry & Anxiety",
        "Strength & Courage",
        "Comfort in Pain & Grief",
        "Hope & New Beginnings",
        "Grace, Forgiveness & Salvation",
        "Who God & Jesus Are",
        "Identity & Purpose",
        "Wisdom, Prayer & Daily Living"
    )
}
