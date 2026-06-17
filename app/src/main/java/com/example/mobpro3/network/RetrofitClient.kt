package com.example.mobpro3.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val BASE_URL =
    "https://backendmobpro3-production.up.railway.app/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object RetrofitClient {

    val apiService: ApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .build()
            .create(ApiService::class.java)
    }
}

enum class ApiStatus {
    LOADING,
    SUCCESS,
    FAILED
}