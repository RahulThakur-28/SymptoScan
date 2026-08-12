package com.rahul.symptoscan.domain.model

import java.text.SimpleDateFormat
import java.util.*

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val isVerified: Boolean = false,
    val memberSince: String,
    val healthScore: Int = 0,
    val assessmentCount: Int = 0,
    // Health Details
    val dob: String? = null,
    val gender: String? = null,
    val bloodGroup: String? = null,
    val height: Double? = null, // in cm
    val weight: Double? = null, // in kg
    val allergies: String? = null,
    val conditions: String? = null,
    val medications: String? = null,
    val isProfileComplete: Boolean = false,
    val emergencyContact: EmergencyContact? = null
) {
    val initials: String
        get() = fullName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")

    val age: Int?
        get() {
            if (dob == null) return null
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val birthDate = sdf.parse(dob) ?: return null
                val today = Calendar.getInstance()
                val birth = Calendar.getInstance()
                birth.time = birthDate
                var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age
            } catch (e: Exception) {
                null
            }
        }

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

data class EmergencyContact(
    val name: String,
    val relationship: String,
    val phoneNumber: String
)
