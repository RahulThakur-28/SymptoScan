package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbEmergencyContact(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("name")
    val name: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    @SerialName("relationship")
    val relationship: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
