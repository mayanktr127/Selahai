package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "selah"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val followups: List<String> = emptyList()
)

data class ChatConversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val guideStyle: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList()
)

class SelahPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("selah_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value).apply()

    var guideStyle: String
        get() = prefs.getString("guide_style", "shepherd") ?: "shepherd"
        set(value) = prefs.edit().putString("guide_style", value).apply()

    var themeMode: String
        get() = prefs.getString("theme_mode", "light") ?: "light"
        set(value) = prefs.edit().putString("theme_mode", value).apply()

    var readAloud: Boolean
        get() = prefs.getBoolean("read_aloud", false)
        set(value) = prefs.edit().putBoolean("read_aloud", value).apply()

    var dailyReminder: Boolean
        get() = prefs.getBoolean("daily_reminder", true)
        set(value) = prefs.edit().putBoolean("daily_reminder", value).apply()

    fun getSavedVerseIds(): Set<Int> {
        val stringSet = prefs.getStringSet("saved_verses", emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleSavedVerse(verseId: Int): Boolean {
        val current = getSavedVerseIds().toMutableSet()
        val isSaved = if (current.contains(verseId)) {
            current.remove(verseId)
            false
        } else {
            current.add(verseId)
            true
        }
        prefs.edit().putStringSet("saved_verses", current.map { it.toString() }.toSet()).apply()
        return isSaved
    }

    fun isVerseSaved(verseId: Int): Boolean {
        return getSavedVerseIds().contains(verseId)
    }

    fun getLikedVerseIds(): Set<Int> {
        val stringSet = prefs.getStringSet("liked_verses", emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleLikedVerse(verseId: Int): Boolean {
        val current = getLikedVerseIds().toMutableSet()
        val isLiked = if (current.contains(verseId)) {
            current.remove(verseId)
            false
        } else {
            current.add(verseId)
            true
        }
        prefs.edit().putStringSet("liked_verses", current.map { it.toString() }.toSet()).apply()
        return isLiked
    }

    // Conversations persistence
    fun getConversations(): List<ChatConversation> {
        val jsonStr = prefs.getString("conversations_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ChatConversation>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val msgArray = obj.optJSONArray("messages") ?: JSONArray()
                val messages = mutableListOf<ChatMessage>()
                for (j in 0 until msgArray.length()) {
                    val mObj = msgArray.getJSONObject(j)
                    val fArray = mObj.optJSONArray("followups") ?: JSONArray()
                    val followups = mutableListOf<String>()
                    for (k in 0 until fArray.length()) {
                        followups.add(fArray.getString(k))
                    }
                    messages.add(
                        ChatMessage(
                            id = mObj.optString("id", UUID.randomUUID().toString()),
                            sender = mObj.getString("sender"),
                            text = mObj.getString("text"),
                            timestamp = mObj.optLong("timestamp", System.currentTimeMillis()),
                            followups = followups
                        )
                    )
                }
                list.add(
                    ChatConversation(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        guideStyle = obj.optString("guideStyle", "shepherd"),
                        lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis()),
                        messages = messages
                    )
                )
            }
            list.sortedByDescending { it.lastUpdated }.take(30)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveConversation(conv: ChatConversation) {
        val existing = getConversations().toMutableList()
        val idx = existing.indexOfFirst { it.id == conv.id }
        if (idx >= 0) {
            existing[idx] = conv
        } else {
            existing.add(0, conv)
        }
        val limited = existing.take(30)
        try {
            val array = JSONArray()
            for (c in limited) {
                val cObj = JSONObject()
                cObj.put("id", c.id)
                cObj.put("title", c.title)
                cObj.put("guideStyle", c.guideStyle)
                cObj.put("lastUpdated", c.lastUpdated)
                val msgArray = JSONArray()
                for (m in c.messages) {
                    val mObj = JSONObject()
                    mObj.put("id", m.id)
                    mObj.put("sender", m.sender)
                    mObj.put("text", m.text)
                    mObj.put("timestamp", m.timestamp)
                    val fArray = JSONArray()
                    m.followups.forEach { fArray.put(it) }
                    mObj.put("followups", fArray)
                    msgArray.put(mObj)
                }
                cObj.put("messages", msgArray)
                array.put(cObj)
            }
            prefs.edit().putString("conversations_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteConversation(id: String) {
        val existing = getConversations().filter { it.id != id }
        try {
            val array = JSONArray()
            for (c in existing) {
                val cObj = JSONObject()
                cObj.put("id", c.id)
                cObj.put("title", c.title)
                cObj.put("guideStyle", c.guideStyle)
                cObj.put("lastUpdated", c.lastUpdated)
                val msgArray = JSONArray()
                for (m in c.messages) {
                    val mObj = JSONObject()
                    mObj.put("id", m.id)
                    mObj.put("sender", m.sender)
                    mObj.put("text", m.text)
                    mObj.put("timestamp", m.timestamp)
                    val fArray = JSONArray()
                    m.followups.forEach { fArray.put(it) }
                    mObj.put("followups", fArray)
                    msgArray.put(mObj)
                }
                cObj.put("messages", msgArray)
                array.put(cObj)
            }
            prefs.edit().putString("conversations_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
