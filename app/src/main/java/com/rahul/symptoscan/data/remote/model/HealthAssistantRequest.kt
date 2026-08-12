package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthAssistantRequest(
    val conversationId: String,
    val message: String,
    val language: String
)
