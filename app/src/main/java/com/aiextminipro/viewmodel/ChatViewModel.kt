package com.aiextminipro.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiextminipro.model.Message
import com.aiextminipro.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    var isNonLogging = false
    var saveHistoryEnabled = true

    init { loadHistory() }

    fun loadHistory() {
        if (saveHistoryEnabled && !isNonLogging) _messages.value = repository.getHistory()
        else _messages.value = emptyList()
    }

    fun setNonLogging(enabled: Boolean) {
        isNonLogging = enabled
        if (enabled) clearAllData()
        else loadHistory()
    }

    fun setSaveHistory(enabled: Boolean) {
        saveHistoryEnabled = enabled
        if (!enabled) { repository.clearHistory(); _messages.value = emptyList() }
        else loadHistory()
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = Message(text, true)
        val current = _messages.value?.toMutableList() ?: mutableListOf()
        current.add(userMsg)
        _messages.value = current
        val apiKey = repository.getApiKey()
        if (apiKey.isEmpty()) { _error.value = "API-Key fehlt"; return }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val reply = repository.sendMessage(text, apiKey, current)
                val assistantMsg = Message(reply, false)
                val newList = current.toMutableList().apply { add(assistantMsg) }
                _messages.value = newList
                if (saveHistoryEnabled && !isNonLogging) repository.saveHistory(newList)
            } catch (e: Exception) { _error.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun clearAllData() { _messages.value = emptyList(); repository.clearHistory() }
    fun setApiKey(key: String) = repository.setApiKey(key)
}
