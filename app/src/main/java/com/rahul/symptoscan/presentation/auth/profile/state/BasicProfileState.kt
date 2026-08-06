package com.rahul.symptoscan.presentation.auth.profile.state

data class BasicProfileState(
    val age: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val bloodGroup: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
) {
    val isContinueEnabled: Boolean
        get() = age.isNotBlank() && gender.isNotBlank() && height.isNotBlank() && weight.isNotBlank()
}