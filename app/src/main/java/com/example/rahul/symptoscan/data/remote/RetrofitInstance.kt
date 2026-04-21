package com.example.rahul.symptoscan.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY   // 🔥 IMPORTANT
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)   // 🔥 ADD THIS
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }
}