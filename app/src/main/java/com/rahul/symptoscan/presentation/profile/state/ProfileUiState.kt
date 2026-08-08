package com.rahul.symptoscan.presentation.profile.state

import com.rahul.symptoscan.domain.model.Achievement
import com.rahul.symptoscan.domain.model.UserProfile

data class ProfileUiState(
    val user: UserProfile? = null,
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
