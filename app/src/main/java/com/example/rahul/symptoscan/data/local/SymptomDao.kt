package com.example.rahul.symptoscan.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomEntity)

    @Query("SELECT * FROM symptom_history ORDER BY date DESC")
    fun getAllSymptoms(): Flow<List<SymptomEntity>>

    @Delete
    suspend fun deleteSymptom(symptom: SymptomEntity)
}