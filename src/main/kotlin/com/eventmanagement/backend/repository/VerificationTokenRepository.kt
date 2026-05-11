package com.eventmanagement.backend.repository

import com.eventmanagement.backend.model.VerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VerificationTokenRepository : JpaRepository<VerificationToken, Long> {

    fun findByToken(token: String): VerificationToken?

    fun findByTokenAndType(token: String, type: String): VerificationToken?

    fun findByUserIdAndType(userId: Long, type: String): List<VerificationToken>

    fun deleteByExpiryDateBefore(LocalDateTime: java.time.LocalDateTime)
}