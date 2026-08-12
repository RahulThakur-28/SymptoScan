package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthAssistantResponseDto(
    val conversationId: String,
    val message: String
)
