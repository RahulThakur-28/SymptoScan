package com.rahul.symptoscan.presentation.auth.profile.event

sealed class BasicProfileEvent {
    data class AgeChanged(val age: String) : BasicProfileEvent()
    data class GenderChanged(val gender: String) : BasicProfileEvent()
    data class HeightChanged(val height: String) : BasicProfileEvent()
    data class WeightChanged(val weight: String) : BasicProfileEvent()
    data class BloodGroupChanged(val bloodGroup: String) : BasicProfileEvent()
    object ContinueClicked : BasicProfileEvent()
}
