package com.aiextminipro.repository
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aiextminipro.model.ChatRequest
import com.aiextminipro.model.ChatResponse
import com.aiextminipro.model.Message
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.security.GeneralSecurityException
import java.util.concurrent.TimeUnit

class ChatRepository(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()
    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(context, "secure_prefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        } catch (e: GeneralSecurityException) {
            context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        }
    }
    fun getApiKey(): String = prefs.getString("api_key", "") ?: ""
    fun setApiKey(key: String) = prefs.edit().putString("api_key", key).apply()
    fun getHistory(): MutableList<Message> {
        val json = prefs.getString("history", "[]") ?: "[]"
        val list = mutableListOf<Message>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(Message(obj.getString("text"), obj.getBoolean("isUser"), obj.getLong("timestamp")))
            }
        } catch (_: Exception) {}
        return list
    }
    fun saveHistory(messages: List<Message>) {
        val arr = JSONArray()
        messages.forEach {
            arr.put(JSONObject().apply {
                put("text", it.text); put("isUser", it.isUser); put("timestamp", it.timestamp)
            })
        }
        prefs.edit().putString("history", arr.toString()).apply()
    }
    fun clearHistory() = prefs.edit().remove("history").apply()
    suspend fun sendMessage(message: String, apiKey: String, history: List<Message>): String {
        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().put("role", "system").put("content", "You are an expert coding assistant."))
        history.forEach {
            messagesArray.put(JSONObject().put("role", if (it.isUser) "user" else "assistant").put("content", it.text))
        }
        messagesArray.put(JSONObject().put("role", "user").put("content", message))
        val body = ChatRequest(BuildConfig.DEFAULT_MODEL, messagesArray, BuildConfig.TEMPERATURE, 1024).toJson()
        val request = Request.Builder()
            .url(BuildConfig.API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(RequestBody.create(MediaType.parse("application/json"), body))
            .build()
        return try {
            val response = client.newCall(request).execute()
            val json = response.body?.string() ?: ""
            if (response.isSuccessful) {
                ChatResponse.fromJson(json).choices.firstOrNull()?.message?.content ?: "Keine Antwort"
            } else { "❌ HTTP ${response.code}: $json" }
        } catch (e: Exception) { "❌ Fehler: ${e.message}" }
    }
}
