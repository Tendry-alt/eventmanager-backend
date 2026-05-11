package com.eventmanagement.backend.dto

data class EventResponse(
    val id: Long,
    val name: String,
    val description: String,
    val eventDate: String,
    val location: String,
    val capacity: Int,
    val createdBy: Long?,
    val createdAt: String,
    val ticketsCount: Long
)