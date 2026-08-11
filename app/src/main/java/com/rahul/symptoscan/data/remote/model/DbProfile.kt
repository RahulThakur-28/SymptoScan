package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbProfile(
    @SerialName("id")
    val id: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
