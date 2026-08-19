package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.data.remote.SupabaseClient
import com.rahul.symptoscan.data.remote.model.DbHealthProfile
import com.rahul.symptoscan.domain.model.HealthProfile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class HealthProfileRepository {

    private val postgrest = SupabaseClient.database

    fun getHealthProfile(userId: String): Flow<HealthProfile?> = flow {
        val dbProfile = try {
            postgrest.from("health_profiles")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<DbHealthProfile>()
        } catch (e: Exception) {
            null
        }

        emit(dbProfile?.let { mapToDomain(it) })
    }

    suspend fun saveHealthProfile(profile: HealthProfile): Result<Unit> = withContext(Dispatchers.IO) {
        android.util.Log.d("HealthProfile", "[HEALTH-SAVE-5] Database save started for user: ${profile.userId}")
        
        runCatching {
            // Fetch current to avoid overwriting existing data with nulls
            val existing = postgrest.from("health_profiles")
                .select() {
                    filter { eq("user_id", profile.userId) }
                }
                .decodeSingleOrNull<DbHealthProfile>()

            if (existing != null) {
                android.util.Log.d("HealthProfile", "[HEALTH-SAVE-5.1] Existing profile found. Merging data.")
            } else {
                android.util.Log.d("HealthProfile", "[HEALTH-SAVE-5.1] No existing profile. Creating new.")
            }

            val merged = DbHealthProfile(
                userId = profile.userId,
                dateOfBirth = profile.dateOfBirth ?: existing?.dateOfBirth,
                biologicalSex = profile.biologicalSex ?: existing?.biologicalSex,
                heightCm = profile.heightCm ?: existing?.heightCm,
                weightKg = profile.weightKg ?: existing?.weightKg,
                bloodGroup = profile.bloodGroup ?: existing?.bloodGroup,
                allergies = profile.allergies ?: existing?.allergies,
                medications = profile.medications ?: existing?.medications,
                medicalConditions = profile.medicalConditions ?: existing?.medicalConditions,
                otherInformation = profile.otherInformation ?: existing?.otherInformation,
                profileCompleted = profile.profileCompleted || (existing?.profileCompleted ?: false)
            )

            postgrest.from("health_profiles").upsert(merged) {
                onConflict = "user_id"
            }
            android.util.Log.d("HealthProfile", "[HEALTH-SAVE-6] Upsert successful")
            Unit
        }.onFailure { e ->
            if (com.rahul.symptoscan.BuildConfig.DEBUG) {
                android.util.Log.e("HealthProfile", "[HEALTH-SAVE-ERROR] Repository failure: ${e.message}")
            }
        }
    }

    suspend fun saveBasicHealthProfile(profile: HealthProfile): Result<Unit> {
        return saveHealthProfile(profile.copy(profileCompleted = false))
    }

    suspend fun saveAdditionalHealthProfile(profile: HealthProfile): Result<Unit> {
        return saveHealthProfile(profile.copy(profileCompleted = true))
    }

    private fun mapToDomain(db: DbHealthProfile): HealthProfile {
        return HealthProfile(
            userId = db.userId,
            dateOfBirth = db.dateOfBirth,
            biologicalSex = db.biologicalSex,
            heightCm = db.heightCm,
            weightKg = db.weightKg,
            bloodGroup = db.bloodGroup,
            allergies = db.allergies,
            medications = db.medications,
            medicalConditions = db.medicalConditions,
            otherInformation = db.otherInformation,
            profileCompleted = db.profileCompleted
        )
    }

    private fun mapToData(domain: HealthProfile): DbHealthProfile {
        return DbHealthProfile(
            userId = domain.userId,
            dateOfBirth = domain.dateOfBirth,
            biologicalSex = domain.biologicalSex,
            heightCm = domain.heightCm,
            weightKg = domain.weightKg,
            bloodGroup = domain.bloodGroup,
            allergies = domain.allergies,
            medications = domain.medications,
            medicalConditions = domain.medicalConditions,
            otherInformation = domain.otherInformation,
            profileCompleted = domain.profileCompleted
        )
    }
}
