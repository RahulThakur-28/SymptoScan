package com.rahul.symptoscan.domain.model

data class HealthConversation(
    val id: String,
    val userId: String,
    val title: String?,
    val language: String,
    val createdAt: String?,
    val updatedAt: String?
)
