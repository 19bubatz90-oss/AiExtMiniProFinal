package com.aiextminipro.model
import org.json.JSONArray
import org.json.JSONObject
data class ChatRequest(val model: String, val messages: JSONArray, val temperature: Double, val maxTokens: Int) {
    fun toJson(): String = JSONObject().apply {
        put("model", model); put("messages", messages); put("temperature", temperature); put("max_tokens", maxTokens)
    }.toString()
}
