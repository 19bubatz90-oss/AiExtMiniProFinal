package com.aiextminipro.model
data class Message(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())
