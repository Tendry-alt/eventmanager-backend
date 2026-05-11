package com.eventmanagement.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "verification_tokens")
data class VerificationToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var token: String = "",

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(nullable = false)
    var type: String = "",

    @Column(name = "expiry_date", nullable = false)
    var expiryDate: LocalDateTime = LocalDateTime.now(),

    var used: Boolean = false,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
)