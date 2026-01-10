package com.example.booknpay.data.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: String = "", // 'booking', 'payment', 'schedule', 'promotion', 'system'
    val title: String = "",
    val message: String = "",
    val data: Map<String, String> = emptyMap(),
    val actionUrl: String? = null,
    val priority: String = "medium", // 'low', 'medium', 'high'
    val isRead: Boolean = false,
    val readAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now()
)
