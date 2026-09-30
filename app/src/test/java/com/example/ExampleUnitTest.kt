package com.example

import com.example.data.VerseConstants
import com.example.data.VerseRepository
import com.example.ui.components.FollowUpParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAll100VersesLoaded() {
        val verses = VerseRepository.getAll()
        assertEquals("Repository must have exactly 100 verses", 100, verses.size)

        // Verify sequential IDs from 1 to 100
        val ids = verses.map { it.id }.toSet()
        for (i in 1..100) {
            assertTrue("Verse $i must exist", ids.contains(i))
        }
    }

    @Test
    fun testPartCounts() {
        var total = 0
        VerseConstants.PARTS.forEach { part ->
            val count = VerseRepository.getCountForPart(part)
            assertTrue("Part '$part' should have verses", count > 0)
            total += count
        }
        assertEquals("Sum of all parts must equal 100", 100, total)
    }

    @Test
    fun testReferenceSearch() {
        val john316 = VerseRepository.getByReference("John 3:16")
        assertNotNull(john316)
        assertEquals("God So Loved the World", john316?.title)

        val psalm23 = VerseRepository.getByReference("Psalm 23:4")
        assertNotNull(psalm23)
        assertEquals("The Valley of the Shadow", psalm23?.title)
    }

    @Test
    fun testReferenceExtractor() {
        val sampleText = "Please reflect on John 3:16 and also Romans 8:38-39 as well as Psalm 23:1."
        val refs = VerseRepository.findReferencesInText(sampleText)
        assertTrue(refs.contains("John 3:16"))
        assertTrue(refs.contains("Romans 8:38-39"))
        assertTrue(refs.contains("Psalm 23:1"))
    }

    @Test
    fun testFollowUpParser() {
        val textWithFollowups = "Here is what this means.\n\n<<FOLLOWUPS: question one | question two | question three>>"
        val (clean, questions) = FollowUpParser.parse(textWithFollowups)

        assertEquals("Here is what this means.", clean)
        assertEquals(3, questions.size)
        assertEquals("question one", questions[0])
        assertEquals("question two", questions[1])
        assertEquals("question three", questions[2])
    }

    @Test
    fun testMoodTopics() {
        assertEquals(12, VerseConstants.TOPICS.size)
        VerseConstants.TOPICS.forEach { topic ->
            assertTrue("Topic ${topic.id} has associated verse IDs", topic.verseIds.isNotEmpty())
            val verses = VerseRepository.getVersesForTopic(topic.id)
            assertTrue("Verses found for topic ${topic.id}", verses.isNotEmpty())
        }
    }
}
