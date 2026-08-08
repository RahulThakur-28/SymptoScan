package com.rahul.symptoscan.domain.model

data class Symptom(
    val id: String,
    val name: String,
    val icon: String, // Emoji or resource name
    val category: String,
    val isSelected: Boolean = false
)
