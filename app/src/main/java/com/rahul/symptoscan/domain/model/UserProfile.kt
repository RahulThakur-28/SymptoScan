package com.rahul.symptoscan.domain.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val isVerified: Boolean = false,
    val memberSince: String,
    val healthScore: Int = 0,
    val assessmentCount: Int = 0,
    // Health Details
    val age: Int? = null,
    val dob: String? = null,
    val gender: String? = null,
    val bloodGroup: String? = null,
    val height: Int? = null, // in cm
    val weight: Int? = null, // in kg
    val allergies: List<String> = emptyList()
) {
    val initials: String
        get() = fullName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")

    val bmi: Double?
        get() = if (height != null && weight != null && height > 0) {
            val heightInMeters = height / 100.0
            weight / (heightInMeters * heightInMeters)
        } else null
        
    val bmiStatus: String
        get() {
            val valBmi = bmi ?: return "Unknown"
            return when {
                valBmi < 18.5 -> "Underweight"
                valBmi < 25.0 -> "Normal"
                valBmi < 30.0 -> "Overweight"
                else -> "Obese"
            }
        }
}
