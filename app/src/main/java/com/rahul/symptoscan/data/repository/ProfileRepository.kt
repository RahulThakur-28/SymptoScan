package com.rahul.symptoscan.data.repository

import android.util.Log
import com.rahul.symptoscan.BuildConfig
import com.rahul.symptoscan.data.remote.SupabaseClient
import com.rahul.symptoscan.data.remote.model.DbProfile
import com.rahul.symptoscan.domain.model.Achievement
import com.rahul.symptoscan.domain.model.UserProfile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonPrimitive

/**
 * Repository to manage user profile and settings data using Supabase Postgrest.
 */
class ProfileRepository {

    private val postgrest = SupabaseClient.database
    private val auth = SupabaseClient.auth
    private val TAG = "ProfileRepository"

    /**
     * Fetches the user profile from Supabase 'profiles' table.
     */
    fun getUserProfile(userId: String): Flow<UserProfile?> = flow {
        val dbProfile = try {
            postgrest.from("profiles")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<DbProfile>()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error fetching profile for $userId: ${e.message}", e)
            }
            null
        }

        emit(dbProfile?.let { mapToDomain(it) })
    }

    /**
     * Ensures that a profile exists for the current authenticated user.
     * If it doesn't exist, creates one using the metadata from Auth.
     */
    suspend fun ensureUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        val user = auth.currentUserOrNull()
            ?: return@withContext Result.failure(IllegalStateException("Authenticated user not found"))

        val fullName = user.userMetadata?.get("full_name")?.jsonPrimitive?.content
        if (fullName.isNullOrBlank()) {
            val error = "User full_name metadata is missing"
            if (BuildConfig.DEBUG) Log.e(TAG, error)
            return@withContext Result.failure(IllegalStateException(error))
        }

        val id = user.id

        runCatching {
            // STEP 3: Check existing profile
            if (BuildConfig.DEBUG) Log.d(TAG, "Checking if profile exists for $id...")

            val dbProfile = postgrest.from("profiles")
                .select(columns = Columns.ALL) {
                    filter { eq("id", id) }
                }
                .decodeSingleOrNull<DbProfile>()

            if (dbProfile != null) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Profile already exists for $id")
                mapToDomain(dbProfile)
            } else {
                // STEP 4: Insert
                if (BuildConfig.DEBUG) Log.d(TAG, "Profile not found. Attempting to INSERT for ID: $id, Name: $fullName")

                val newProfile = DbProfile(id = id, fullName = fullName)
                
                try {
                    postgrest.from("profiles").insert(newProfile)
                    if (BuildConfig.DEBUG) Log.d(TAG, "INSERT successful for $id")
                } catch (e: Exception) {
                    Log.e(TAG, "Profile INSERT failed for $id: ${e.message}", e)
                    // If insert fails, check if it's because it already exists (concurrency)
                    val retryProfile = postgrest.from("profiles")
                        .select { filter { eq("id", id) } }
                        .decodeSingleOrNull<DbProfile>()
                    if (retryProfile != null) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Profile found on retry SELECT after INSERT failure")
                        return@runCatching mapToDomain(retryProfile)
                    }
                    throw e
                }
                
                // Final SELECT to return the final object from DB
                val finalProfile = postgrest.from("profiles")
                    .select { filter { eq("id", id) } }
                    .decodeSingleOrNull<DbProfile>()
                    ?: throw IllegalStateException("Profile was not created")
                
                mapToDomain(finalProfile)
            }
        }.onFailure { e ->
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "ensureUserProfile critical failure for $id", e)
            }
        }
    }

    /**
     * Updates the user profile in Supabase.
     */
    suspend fun updateProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dbProfile = DbProfile(
                id = profile.id,
                fullName = profile.fullName,
                avatarUrl = null // Can be added later
            )
            postgrest.from("profiles").update(dbProfile) {
                filter { eq("id", profile.id) }
            }
            Unit
        }
    }

    private fun mapToDomain(dbProfile: DbProfile): UserProfile {
        return UserProfile(
            id = dbProfile.id,
            fullName = dbProfile.fullName,
            email = "", // Email comes from Auth, not public.profiles as per task scope
            isVerified = true,
            memberSince = dbProfile.createdAt ?: "Recently",
            healthScore = 0,
            assessmentCount = 0
        )
    }

    /**
     * Fetches user achievements.
     */
    fun getAchievements(): Flow<List<Achievement>> = flow {
        kotlinx.coroutines.delay(800)
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
