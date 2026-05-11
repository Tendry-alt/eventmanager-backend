package com.eventmanagement.backend.controller

import com.eventmanagement.backend.model.Event
import com.eventmanagement.backend.model.User
import com.eventmanagement.backend.repository.EventRepository
import com.eventmanagement.backend.repository.UserRepository
import com.eventmanagement.backend.repository.TicketRepository
import com.eventmanagement.backend.service.EmailService
import com.eventmanagement.backend.service.HashService
import com.eventmanagement.backend.service.TokenService
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Controller
@RequestMapping("/admin")
class AdminWebController(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val ticketRepository: TicketRepository,
    private val hashService: HashService,
    private val emailService: EmailService,
    private val tokenService: TokenService
) {

    @Value("\${app.base.url}")
    private lateinit var baseUrl: String

    // =====================================================
    // DASHBOARD
    // =====================================================

    @GetMapping
    fun dashboard(model: Model, session: HttpSession): String {
        checkAdmin(session)

        val stats = mapOf(
            "eventsCount" to eventRepository.count(),
            "usersCount" to userRepository.count(),
            "ticketsCount" to ticketRepository.count(),
            "usedTicketsCount" to ticketRepository.countByUsedTrue()
        )

        val eventNames = eventRepository.findAll().map { it.name }
        val eventTicketCounts = eventRepository.findAll().map { event ->
            ticketRepository.countByEventId(event.id!!)
        }

        val recentTickets = ticketRepository.findTop10ByOrderByGeneratedAtDesc()
            .map { ticket ->
                mapOf(
                    "id" to ticket.id,
                    "eventName" to eventRepository.findById(ticket.eventId).get().name,
                    "userEmail" to userRepository.findById(ticket.userId).get().email,
                    "used" to ticket.used
                )
            }

        model.addAttribute("stats", stats)
        model.addAttribute("eventNames", eventNames)
        model.addAttribute("eventTicketCounts", eventTicketCounts)
        model.addAttribute("recentTickets", recentTickets)

        return "admin/dashboard"
    }

    // =====================================================
    // EVENEMENTS
    // =====================================================

    @GetMapping("/events")
    fun events(model: Model, session: HttpSession): String {
        checkAdmin(session)
        val events = eventRepository.findAll().map { event ->
            mapOf(
                "id" to event.id,
                "name" to event.name,
                "description" to event.description,
                "eventDate" to event.eventDate,
                "location" to event.location,
                "capacity" to event.capacity,
                "ticketsCount" to ticketRepository.countByEventId(event.id!!)
            )
        }
        model.addAttribute("events", events)
        return "admin/events"
    }

    @PostMapping("/events/create")
    fun createEvent(@ModelAttribute event: Event, session: HttpSession): String {
        checkAdmin(session)
        event.createdAt = LocalDateTime.now()
        eventRepository.save(event)
        return "redirect:/admin/events"
    }

    @GetMapping("/events/{id}")
    @ResponseBody
    fun getEvent(@PathVariable id: Long, session: HttpSession): Event {
        checkAdmin(session)
        return eventRepository.findById(id).orElseThrow()
    }

    @PostMapping("/events/update")
    fun updateEvent(@ModelAttribute event: Event, session: HttpSession): String {
        checkAdmin(session)
        val existingEvent = eventRepository.findById(event.id!!).get()
        existingEvent.name = event.name
        existingEvent.description = event.description
        existingEvent.eventDate = event.eventDate
        existingEvent.location = event.location
        existingEvent.capacity = event.capacity
        eventRepository.save(existingEvent)
        return "redirect:/admin/events"
    }

    @GetMapping("/events/delete/{id}")
    fun deleteEvent(@PathVariable id: Long, session: HttpSession): String {
        checkAdmin(session)
        eventRepository.deleteById(id)
        return "redirect:/admin/events"
    }

    // =====================================================
    // UTILISATEURS
    // =====================================================

    @GetMapping("/users")
    fun users(
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) enabled: String?,
        model: Model,
        session: HttpSession
    ): String {
        checkAdmin(session)

        var usersList = userRepository.findAll()

        if (!email.isNullOrBlank()) {
            usersList = usersList.filter { it.email.contains(email, ignoreCase = true) }
        }

        if (!role.isNullOrBlank()) {
            usersList = usersList.filter { it.role.equals(role, ignoreCase = true) }
        }

        if (!enabled.isNullOrBlank()) {
            val enabledBool = enabled.toBoolean()
            usersList = usersList.filter { it.enabled == enabledBool }
        }

        val users = usersList.map { user ->
            mapOf(
                "id" to user.id,
                "email" to user.email,
                "role" to user.role,
                "enabled" to user.enabled,
                "createdAt" to user.createdAt
            )
        }

        model.addAttribute("users", users)
        model.addAttribute("filterEmail", email ?: "")
        model.addAttribute("filterRole", role ?: "")
        model.addAttribute("filterEnabled", enabled ?: "")
        return "admin/users"
    }

    @PostMapping("/users/create")
    fun createUser(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam role: String,
        @RequestParam enabled: Boolean,
        session: HttpSession
    ): String {
        checkAdmin(session)

        val user = User().apply {
            this.email = email
            this.passwordHash = hashService.hashPassword(password)
            this.role = role
            this.enabled = enabled
            this.createdAt = LocalDateTime.now()
        }
        userRepository.save(user)
        return "redirect:/admin/users"
    }

    @PostMapping("/users/update-role")
    @ResponseBody
    fun updateUserRole(@RequestParam userId: Long, @RequestParam role: String, session: HttpSession): Map<String, String> {
        checkAdmin(session)
        val user = userRepository.findById(userId).orElseThrow()
        user.role = role
        userRepository.save(user)
        return mapOf("status" to "success")
    }

    @PostMapping("/users/approve/{id}")
    @ResponseBody
    fun approveUser(@PathVariable id: Long, session: HttpSession): Map<String, String> {
        checkAdmin(session)
        val user = userRepository.findById(id).orElseThrow()
        val token = tokenService.createRegistrationToken(user.id!!)
        emailService.sendVerificationEmail(user.email, token.token, baseUrl)
        return mapOf("status" to "success", "message" to "Email de validation envoyé")
    }

    @PostMapping("/users/disable/{id}")
    @ResponseBody
    fun disableUser(@PathVariable id: Long, session: HttpSession): Map<String, String> {
        checkAdmin(session)
        val user = userRepository.findById(id).orElseThrow()
        user.enabled = false
        userRepository.save(user)
        return mapOf("status" to "success")
    }

    @GetMapping("/users/delete/{id}")
    fun deleteUser(@PathVariable id: Long, session: HttpSession): String {
        checkAdmin(session)
        userRepository.deleteById(id)
        return "redirect:/admin/users"
    }

    // =====================================================
    // TICKETS
    // =====================================================

    @GetMapping("/tickets")
    fun tickets(
        @RequestParam(required = false) eventId: Long?,
        @RequestParam(required = false) used: String?,
        model: Model,
        session: HttpSession
    ): String {
        checkAdmin(session)

        val allEvents = eventRepository.findAll()
        model.addAttribute("events", allEvents)
        model.addAttribute("filterEventId", eventId)
        model.addAttribute("filterUsed", used)

        var ticketsList = ticketRepository.findAll()

        if (eventId != null) {
            ticketsList = ticketsList.filter { it.eventId == eventId }
        }

        if (used != null) {
            val usedBool = used.toBoolean()
            ticketsList = ticketsList.filter { it.used == usedBool }
        }

        val tickets = ticketsList.map { ticket ->
            mapOf(
                "id" to ticket.id,
                "eventName" to eventRepository.findById(ticket.eventId).get().name,
                "userEmail" to userRepository.findById(ticket.userId).get().email,
                "used" to ticket.used,
                "validatedByName" to (ticket.validatedBy?.let { userRepository.findById(it).get().email }),
                "generatedAt" to ticket.generatedAt,
                "validatedAt" to ticket.validatedAt
            )
        }

        model.addAttribute("tickets", tickets)
        return "admin/tickets"
    }

    // Nouvelle méthode pour supprimer un ticket
    @DeleteMapping("/tickets/delete/{id}")
    @ResponseBody
    fun deleteTicket(@PathVariable id: Long, session: HttpSession): ResponseEntity<Map<String, String>> {
        checkAdmin(session)

        if (!ticketRepository.existsById(id)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("status" to "error", "message" to "Ticket non trouvé"))
        }

        ticketRepository.deleteById(id)
        return ResponseEntity.ok(mapOf("status" to "success", "message" to "Ticket supprimé"))
    }

    // =====================================================
    // PRIVATE
    // =====================================================

    private fun checkAdmin(session: HttpSession) {
        val role = session.getAttribute("adminRole")
        if (role != "admin") {
            throw RuntimeException("Non autorisé")
        }
    }
}