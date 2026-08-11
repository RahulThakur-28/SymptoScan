package com.rahul.symptoscan.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbHealthProfile(
    @SerialName("user_id")
    val userId: String,
    @SerialName("date_of_birth")
    val dateOfBirth: String? = null,
    @SerialName("biological_sex")
    val biologicalSex: String? = null,
    @SerialName("height_cm")
    val heightCm: Double? = null,
    @SerialName("weight_kg")
    val weightKg: Double? = null,
    @SerialName("blood_group")
    val bloodGroup: String? = null,
    @SerialName("allergies")
    val allergies: String? = null,
    @SerialName("medications")
    val medications: String? = null,
    @SerialName("medical_conditions")
    val medicalConditions: String? = null,
    @SerialName("other_information")
    val otherInformation: String? = null,
    @SerialName("profile_completed")
    val profileCompleted: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
