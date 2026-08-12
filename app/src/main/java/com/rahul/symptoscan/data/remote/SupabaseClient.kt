package com.rahul.symptoscan.data.remote

import com.rahul.symptoscan.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json

/**
 * Singleton responsible for initializing and providing the Supabase client.
 */
object SupabaseClient {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {

        install(Auth) {
            alwaysAutoRefresh = true
            autoLoadFromStorage = true
        }

        install(Postgrest)

        install(Functions)

        install(Storage)

        defaultSerializer = KotlinXSerializer(json)
    }

    val auth
        get() = supabase.auth

    val database
        get() = supabase.postgrest

    val storage
        get() = supabase.storage
}
