package com.eventmanagement.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "events")
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    var description: String = "",

    @Column(name = "event_date", nullable = false)
    var eventDate: LocalDateTime = LocalDateTime.now(),

    var location: String = "",

    @Column(nullable = false)
    var capacity: Int = 0,

    @Column(name = "created_by")
    var createdBy: Long? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
)