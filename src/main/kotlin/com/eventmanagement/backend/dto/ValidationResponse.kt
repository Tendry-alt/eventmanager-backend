package com.eventmanagement.backend.dto

data class ValidationResponse(
    val authorized: Boolean,
    val message: String,
    val ticketId: Long? = null,
    val eventName: String? = null
)