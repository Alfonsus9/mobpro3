package com.example.mobpro3.model

data class GoogleLoginResponse(
    val token: String,
    val user: UserResponse
)