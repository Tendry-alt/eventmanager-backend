package com.eventmanagement.backend.controller

import com.eventmanagement.backend.dto.EventResponse
import com.eventmanagement.backend.model.Event
import com.eventmanagement.backend.repository.EventRepository
import com.eventmanagement.backend.repository.TicketRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = ["*"])
class EventController(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository
) {

    @GetMapping
    fun getAllEvents(): List<EventResponse> {
        return eventRepository.findAll().map { event ->
            EventResponse(
                id = event.id!!,
                name = event.name,
                description = event.description,
                eventDate = event.eventDate.toString(),
                location = event.location,
                capacity = event.capacity,
                createdBy = event.createdBy,
                createdAt = event.createdAt.toString(),
                ticketsCount = ticketRepository.countByEventId(event.id!!)
            )
        }
    }

    @GetMapping("/{id}")
    fun getEventById(@PathVariable id: Long): ResponseEntity<EventResponse> {
        val event = eventRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()

        val response = EventResponse(
            id = event.id!!,
            name = event.name,
            description = event.description,
            eventDate = event.eventDate.toString(),
            location = event.location,
            capacity = event.capacity,
            createdBy = event.createdBy,
            createdAt = event.createdAt.toString(),
            ticketsCount = ticketRepository.countByEventId(event.id!!)
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createEvent(@RequestBody event: Event): ResponseEntity<Event> {
        val savedEvent = eventRepository.save(event)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent)
    }

    @PutMapping("/{id}")
    fun updateEvent(@PathVariable id: Long, @RequestBody event: Event): ResponseEntity<Event> {
        if (!eventRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        event.id = id
        val updatedEvent = eventRepository.save(event)
        return ResponseEntity.ok(updatedEvent)
    }

    @DeleteMapping("/{id}")
    fun deleteEvent(@PathVariable id: Long): ResponseEntity<Void> {
        if (!eventRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }
        eventRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}