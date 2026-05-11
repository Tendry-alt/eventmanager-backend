package com.eventmanagement.backend.dto

data class ValidationRequest(
    val qrCodeData: String,
    val agentId: Long
)