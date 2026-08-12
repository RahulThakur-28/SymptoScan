package com.rahul.symptoscan.presentation.emergency.state

import com.rahul.symptoscan.domain.model.EmergencyContact

data class EmergencyUiState(
    val emergencyContact: EmergencyContact? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
