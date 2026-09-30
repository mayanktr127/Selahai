package com.example.data

import java.util.Calendar

object VerseRepository {
    val allVerses: List<Verse> = (versesGroup1 + versesGroup2).also { list ->
        if (list.size != 100) {
            val existingIds = list.map { it.id }.toSet()
            val missing = (1..100).filter { it !in existingIds }
            android.util.Log.w("VerseRepository", "Warning: parsed ${list.size} verses instead of 100. Missing: $missing")
        }
    }

    fun getAll(): List<Verse> = allVerses

    fun getById(id: Int): Verse? = allVerses.find { it.id == id }

    fun getByReference(reference: String): Verse? {
        val normalized = reference.trim().lowercase()
        return allVerses.find { it.reference.lowercase() == normalized }
    }

    fun getByPart(part: String): List<Verse> {
        if (part.equals("All", ignoreCase = true) || part.equals("All 100", ignoreCase = true)) {
            return allVerses
        }
        return allVerses.filter { it.part.equals(part, ignoreCase = true) }
    }

    fun getCountForPart(part: String): Int {
        return allVerses.count { it.part.equals(part, ignoreCase = true) }
    }

    fun search(query: String): List<Verse> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return allVerses
        return allVerses.filter { verse ->
            verse.reference.lowercase().contains(q) ||
            verse.title.lowercase().contains(q) ||
            verse.text.lowercase().contains(q) ||
            verse.meaning.lowercase().contains(q) ||
            verse.themes.any { it.lowercase().contains(q) }
        }
    }

    fun getVerseOfTheDay(): Verse {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear % allVerses.size).coerceIn(0, allVerses.size - 1)
        return allVerses[index]
    }

    fun getVersesForTopic(topicId: String): List<Verse> {
        val topic = VerseConstants.TOPICS.find { it.id == topicId } ?: return emptyList()
        return topic.verseIds.mapNotNull { getById(it) }
    }

    /**
     * Finds Bible references like "John 3:16", "Romans 8:38-39", "1 Corinthians 13:4-7", "Psalm 23:4"
     */
    private val referenceRegex = Regex("""\b(?:[1-3]\s)?[A-Z][a-zA-Z]+(?:\s[A-Z][a-zA-Z]+)?\s\d+:\d+(?:-\d+)?\b""")

    fun findReferencesInText(text: String): List<String> {
        return referenceRegex.findAll(text).map { it.value }.toList()
    }
}
