package com.example.data

data class LlmModel(
    val id: String,
    val name: String,
    val provider: String,
    val badge: String,
    val description: String,
    val speedTag: String,
    val isProOnly: Boolean = false,
    val guideStyleKey: String
)

object AvailableModels {
    val models = listOf(
        LlmModel(
            id = "claude-sonnet",
            name = "Claude 3.5 Sonnet",
            provider = "Anthropic Style",
            badge = "Deep Contemplative",
            description = "Nuanced, poetic biblical theology with profound pastoral depth and empathetic reflection.",
            speedTag = "Thoughtful",
            guideStyleKey = "shepherd"
        ),
        LlmModel(
            id = "gemini-flash",
            name = "Gemini 3.5 Flash",
            provider = "Google AI",
            badge = "Instant Grounded",
            description = "Lightning fast (under 1.2s delay), direct Scripture citations, modern life application.",
            speedTag = "Fastest • 1.2s",
            guideStyleKey = "friend"
        ),
        LlmModel(
            id = "gpt-scholar",
            name = "GPT-4o Scholar",
            provider = "OpenAI Style",
            badge = "Exegetical Deep-Dive",
            description = "Original Greek and Hebrew word roots, historical context, and covenant theology.",
            speedTag = "Academic",
            guideStyleKey = "scholar"
        ),
        LlmModel(
            id = "selah-storyteller",
            name = "Selah Storyteller",
            provider = "Narrative Engine",
            badge = "Vivid Narrative",
            description = "Transports you into the scene, unpacking the dramatic history and lives of Scripture.",
            speedTag = "Immersive",
            guideStyleKey = "storyteller"
        ),
        LlmModel(
            id = "selah-shepherd",
            name = "Selah Shepherd",
            provider = "Pastoral Heart",
            badge = "Gentle Comfort",
            description = "Warm listening, emotional reassurance, ending with an uplifting personalized prayer.",
            speedTag = "Pastoral",
            guideStyleKey = "shepherd"
        )
    )

    fun getById(id: String): LlmModel {
        return models.find { it.id == id } ?: models.first()
    }
}
