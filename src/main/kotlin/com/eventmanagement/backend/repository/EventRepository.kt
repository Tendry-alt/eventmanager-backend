package com.eventmanagement.backend.repository

import com.eventmanagement.backend.model.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {
    fun findByEventDateAfter(date: LocalDateTime): List<Event>
}