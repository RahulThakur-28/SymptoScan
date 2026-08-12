package com.rahul.symptoscan.domain.model

data class EmergencyContact(
    val id: String? = null,
    val userId: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
