package com.rahul.symptoscan.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val progress: Int, // 0-100
    val isUnlocked: Boolean = false
)
