package com.rahul.symptoscan.presentation.healthprofile.state

import com.rahul.symptoscan.domain.model.EmergencyContact
import com.rahul.symptoscan.domain.model.UserProfile

data class HealthProfileUiState(
    val currentStep: Int = 1,
    val userProfile: UserProfile? = null,
    
    // Step 1: Personal
    val fullName: String = "",
    val dob: String = "",
    val gender: String = "",
    
    // Step 2: Body
    val height: String = "",
    val weight: String = "",
    val bloodGroup: String = "",
    
    // Step 3: Medical
    val selectedAllergies: List<String> = emptyList(),
    val conditions: List<String> = emptyList(),
    val medications: String = "",
    
    // Step 4: Emergency
    val emergencyContactName: String = "",
    val emergencyRelationship: String = "",
    val emergencyPhone: String = "",
    
    val profileCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
) {
    val bmi: Double?
        get() {
            val h = height.toDoubleOrNull() ?: return null
            val w = weight.toDoubleOrNull() ?: return null
            if (h <= 0) return null
            val heightInMeters = h / 100.0
            return w / (heightInMeters * heightInMeters)
        }
        
    val bmiStatus: String
        get() {
            val valBmi = bmi ?: return ""
            return when {
                valBmi < 18.5 -> "Underweight"
                valBmi < 25.0 -> "Normal"
                valBmi < 30.0 -> "Overweight"
                else -> "Obese"
            }
        }
}
