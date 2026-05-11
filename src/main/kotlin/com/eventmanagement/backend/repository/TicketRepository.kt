package com.eventmanagement.backend.repository

import com.eventmanagement.backend.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {

    // Récupérer les tickets par utilisateur
    fun findByUserId(userId: Long): List<Ticket>

    // Récupérer les tickets par événement
    fun findByEventId(eventId: Long): List<Ticket>

    // Récupérer un ticket par son hash QR code
    fun findByQrCodeHash(qrCodeHash: String): Ticket?

    // Valider un ticket (le marquer comme utilisé)
    @Modifying
    @Transactional
    @Query("UPDATE Ticket t SET t.used = true, t.validatedBy = :agentId, t.validatedAt = CURRENT_TIMESTAMP WHERE t.id = :ticketId AND t.used = false")
    fun validateTicket(@Param("ticketId") ticketId: Long, @Param("agentId") agentId: Long): Int

    // =====================================================
    // Méthodes pour l'interface web (Phase 5)
    // =====================================================

    // Compter le nombre total de tickets utilisés
    fun countByUsedTrue(): Long

    // Compter le nombre de tickets pour un événement spécifique
    fun countByEventId(eventId: Long): Long

    // Récupérer les 10 derniers tickets (pour le dashboard)
    fun findTop10ByOrderByGeneratedAtDesc(): List<Ticket>

    // Compter les tickets par événement et par statut
    fun countByEventIdAndUsed(eventId: Long, used: Boolean): Long

    // Récupérer les tickets par statut (utilisé ou non)
    fun findByUsed(used: Boolean): List<Ticket>
}