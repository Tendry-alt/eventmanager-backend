package com.eventmanagement.backend.dto

data class LoginRequest(
    val email: String,
    val password: String
)