package com.aiextminipro.model
import org.json.JSONObject
data class ChatResponse(val choices: List<Choice>) {
    data class Choice(val message: MessageContent)
    data class MessageContent(val role: String, val content: String)
    companion object {
        fun fromJson(json: String): ChatResponse {
            val obj = JSONObject(json)
            val arr = obj.getJSONArray("choices")
            val list = mutableListOf<Choice>()
            for (i in 0 until arr.length()) {
                val m = arr.getJSONObject(i).getJSONObject("message")
                list.add(Choice(MessageContent(m.getString("role"), m.getString("content"))))
            }
            return ChatResponse(list)
        }
    }
}
