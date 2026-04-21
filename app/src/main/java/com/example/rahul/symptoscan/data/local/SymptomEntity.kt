package com.example.rahul.symptoscan.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_history")
data class SymptomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val symptoms: String,
    val result: String,
    val riskLevel: String,
    val date: Long = System.currentTimeMillis()
)