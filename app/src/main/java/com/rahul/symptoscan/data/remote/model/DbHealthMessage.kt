package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbHealthMessage(
    @SerialName("id")
    val id: String? = null,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
