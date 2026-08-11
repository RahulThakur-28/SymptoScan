package com.rahul.symptoscan.domain.model

data class HealthProfile(
    val userId: String,
    val dateOfBirth: String? = null,
    val biologicalSex: String? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val bloodGroup: String? = null,
    val allergies: String? = null,
    val medications: String? = null,
    val medicalConditions: String? = null,
    val otherInformation: String? = null,
    val profileCompleted: Boolean = false
)
