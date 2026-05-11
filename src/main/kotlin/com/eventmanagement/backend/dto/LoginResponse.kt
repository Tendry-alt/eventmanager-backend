package com.eventmanagement.backend.dto

// dto/LoginResponse.kt
data class LoginResponse(
    val token: String,
    val email: String,
    val role: String,
    val userId: Long? = null
)