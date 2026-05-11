package com.eventmanagement.backend.controller

import com.eventmanagement.backend.dto.TicketGenerationRequest
import com.eventmanagement.backend.dto.ValidationRequest
import com.eventmanagement.backend.dto.ValidationResponse
import com.eventmanagement.backend.model.Ticket
import com.eventmanagement.backend.repository.EventRepository
import com.eventmanagement.backend.repository.TicketRepository
import com.eventmanagement.backend.repository.UserRepository
import com.eventmanagement.backend.service.QrCodeService
import com.eventmanagement.backend.service.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID
import java.security.SecureRandom

@RestController
@RequestMapping("/api/tickets")
class TicketController(
    private val ticketRepository: TicketRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val qrCodeService: QrCodeService,
    private val jwtService: JwtService
) {

    @PostMapping("/generate")
    fun generateTicket(
        @RequestBody request: TicketGenerationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {

        val token = authHeader.substring(7)
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Token invalide"))
        }

        val event = eventRepository.findById(request.eventId)
        if (event.isEmpty) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Événement non trouvé"))
        }

        val user = userRepository.findById(request.userId)
        if (user.isEmpty) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Utilisateur non trouvé"))
        }

        // =====================================================
        // VÉRIFICATION DE LA CAPACITÉ
        // =====================================================
        val currentEvent = event.get()
        val ticketsCount = ticketRepository.countByEventId(currentEvent.id!!)

        if (ticketsCount >= currentEvent.capacity) {
            println("=== CAPACITÉ ATTEINTE ===")
            println("Événement: ${currentEvent.name}")
            println("Capacité: ${currentEvent.capacity}")
            println("Tickets générés: $ticketsCount")

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf(
                    "error" to "EVENT_FULL",
                    "message" to "Cet événement a atteint sa capacité maximale de ${currentEvent.capacity} personnes."
                ))
        }
        // =====================================================

        // Génération d'un QR code unique
        val uniqueId = "${System.currentTimeMillis()}-${UUID.randomUUID()}-${SecureRandom().nextInt(1000000)}"
        val ticketData = "${request.eventId}|${request.userId}|$uniqueId"

        println("=== GÉNÉRATION NOUVEAU TICKET ===")
        println("Ticket data unique: $ticketData")
        println("Places restantes: ${currentEvent.capacity - ticketsCount - 1}")

        val qrCodeBase64 = qrCodeService.generateQrCodeBase64(ticketData)

        val ticket = Ticket().apply {
            eventId = request.eventId
            userId = request.userId
            qrCodeHash = ticketData
            used = false
            generatedAt = LocalDateTime.now()
        }

        val savedTicket = ticketRepository.save(ticket)

        println("Ticket sauvegardé avec ID: ${savedTicket.id}, used: ${savedTicket.used}")

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf(
                "ticketId" to savedTicket.id,
                "qrCode" to qrCodeBase64,
                "message" to "Ticket généré avec succès"
            ))
    }

    @PostMapping("/validate")
    fun validateTicket(
        @RequestBody request: ValidationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<ValidationResponse> {

        val token = authHeader.substring(7)
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ValidationResponse(
                    authorized = false,
                    message = "Token invalide"
                ))
        }

        val role = jwtService.extractRole(token)
        if (role != "agent" && role != "admin") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ValidationResponse(
                    authorized = false,
                    message = "Accès refusé"
                ))
        }

        println("=== VALIDATION TICKET ===")
        println("QR Code reçu: ${request.qrCodeData}")

        val ticket = ticketRepository.findByQrCodeHash(request.qrCodeData)

        if (ticket == null) {
            println("Ticket non trouvé")
            return ResponseEntity.ok(ValidationResponse(
                authorized = false,
                message = "Ticket invalide"
            ))
        }

        println("Ticket trouvé - ID: ${ticket.id}, used: ${ticket.used}")

        if (ticket.used) {
            println("Ticket déjà utilisé")
            return ResponseEntity.ok(ValidationResponse(
                authorized = false,
                message = "Ticket déjà utilisé"
            ))
        }

        println("Validation du ticket...")
        ticket.used = true
        ticket.validatedBy = request.agentId
        ticket.validatedAt = LocalDateTime.now()
        ticketRepository.save(ticket)

        val event = eventRepository.findById(ticket.eventId)
        val eventName = event.map { it.name }.orElse("Inconnu")

        return ResponseEntity.ok(ValidationResponse(
            authorized = true,
            message = "Accès autorisé",
            ticketId = ticket.id,
            eventName = eventName
        ))
    }
}