package com.eventmanagement.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tickets")
data class Ticket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "event_id", nullable = false)
    var eventId: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(name = "qr_code_hash")
    var qrCodeHash: String? = null,

    var used: Boolean = false,

    @Column(name = "validated_by")
    var validatedBy: Long? = null,

    @Column(name = "generated_at")
    var generatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "validated_at")
    var validatedAt: LocalDateTime? = null
)