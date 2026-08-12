package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthAssistantRequest(
    @SerialName("conversationId")
    val conversationId: String,
    @SerialName("message")
    val message: String,
    @SerialName("language")
    val language: String
)
