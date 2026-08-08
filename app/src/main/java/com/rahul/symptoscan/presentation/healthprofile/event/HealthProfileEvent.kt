package com.rahul.symptoscan.presentation.healthprofile.event

sealed class HealthProfileEvent {
    data class StepChanged(val step: Int) : HealthProfileEvent()
    
    // Step 1
    data class FullNameChanged(val name: String) : HealthProfileEvent()
    data class DobChanged(val dob: String) : HealthProfileEvent()
    data class GenderChanged(val gender: String) : HealthProfileEvent()
    
    // Step 2
    data class HeightChanged(val height: String) : HealthProfileEvent()
    data class WeightChanged(val weight: String) : HealthProfileEvent()
    data class BloodGroupChanged(val group: String) : HealthProfileEvent()
    
    // Step 3
    data class AllergyToggled(val allergy: String) : HealthProfileEvent()
    data class CustomAllergyAdded(val allergy: String) : HealthProfileEvent()
    data class ConditionAdded(val condition: String) : HealthProfileEvent()
    data class ConditionRemoved(val condition: String) : HealthProfileEvent()
    data class MedicationsChanged(val meds: String) : HealthProfileEvent()
    
    // Step 4
    data class EmergencyContactNameChanged(val name: String) : HealthProfileEvent()
    data class EmergencyRelationshipChanged(val rel: String) : HealthProfileEvent()
    data class EmergencyPhoneChanged(val phone: String) : HealthProfileEvent()
    
    object Back : HealthProfileEvent()
    object Continue : HealthProfileEvent()
    object Complete : HealthProfileEvent()
}
