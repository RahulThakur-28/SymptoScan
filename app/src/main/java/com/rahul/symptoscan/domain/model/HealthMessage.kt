package com.rahul.symptoscan.domain.model

data class HealthMessage(
    val id: String,
    val conversationId: String,
    val userId: String,
    val role: String,
    val content: String,
    val createdAt: String?
)
