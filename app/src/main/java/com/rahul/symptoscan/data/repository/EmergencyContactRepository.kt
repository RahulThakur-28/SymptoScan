package com.rahul.symptoscan.data.repository

import com.rahul.symptoscan.data.remote.SupabaseClient
import com.rahul.symptoscan.data.remote.model.DbEmergencyContact
import com.rahul.symptoscan.domain.model.EmergencyContact
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencyContactRepository {

    private val postgrest = SupabaseClient.database
    private val auth = SupabaseClient.auth

    private fun DbEmergencyContact.asDomain() = EmergencyContact(
        id = id,
        userId = userId,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun EmergencyContact.asData() = DbEmergencyContact(
        id = id,
        userId = userId,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship
    )

    suspend fun getEmergencyContact(): Result<EmergencyContact?> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
            postgrest.from("emergency_contacts")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<DbEmergencyContact>()?.asDomain()
        }
    }

    suspend fun saveEmergencyContact(contact: EmergencyContact): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            postgrest.from("emergency_contacts").upsert(contact.asData()) {
                onConflict = "user_id"
            }
            Unit
        }
    }

    suspend fun deleteEmergencyContact(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val userId = auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
            postgrest.from("emergency_contacts").delete {
                filter { eq("user_id", userId) }
            }
            Unit
        }
    }
}
