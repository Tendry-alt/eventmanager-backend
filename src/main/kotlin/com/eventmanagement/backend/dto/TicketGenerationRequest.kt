package com.eventmanagement.backend.dto

data class TicketGenerationRequest(
    val eventId: Long,
    val userId: Long
)