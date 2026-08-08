package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.domain.model.Achievement
import com.rahul.symptoscan.domain.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository to manage user profile and settings data.
 */
class ProfileRepository {

    /**
     * Fetches the user profile from Supabase/DB.
     */
    fun getUserProfile(userId: String): Flow<UserProfile> = flow {
        // In a real app, this would query Supabase 'profiles' table.
        // For now, we simulate with data matching the reference.
        delay(1000)
        emit(
            UserProfile(
                id = userId,
                fullName = "Sarah Johnson",
                email = "sarah.johnson@example.com",
                isVerified = true,
                memberSince = "Aug '24",
                healthScore = 82,
                assessmentCount = 12,
                age = 32,
                gender = "Female",
                bloodGroup = "O+",
                height = 165,
                weight = 62,
                allergies = listOf("Penicillin", "Peanuts"),
                conditions = listOf("Type 2 Diabetes", "Mild Hypertension"),
                medications = "Metformin 500mg daily",
                emergencyContact = com.rahul.symptoscan.domain.model.EmergencyContact(
                    name = "James Johnson",
                    relationship = "Spouse",
                    phoneNumber = "+1 (555) 123-4567"
                )
            )
        )
    }

    /**
     * Updates the user profile.
     */
    suspend fun updateProfile(profile: UserProfile): Result<Unit> {
        delay(1000)
        return Result.success(Unit)
    }

    /**
     * Fetches user achievements.
     */
    fun getAchievements(): Flow<List<Achievement>> = flow {
        delay(800)
        emit(
            listOf(
                Achievement("1", "Health Tracker", "10 assessments", "📈", 100, true),
                Achievement("2", "Hydration Hero", "7 day streak", "💧", 60, false),
                Achievement("3", "Profile Complete", "100% filled", "✅", 100, true),
                Achievement("4", "Safety First", "Emergency set", "🛡️", 100, true)
            )
        )
    }
}
