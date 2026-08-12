package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthAssistantResponseDto(
    @SerialName("conversationId")
    val conversationId: String,
    @SerialName("message")
    val message: String
)
