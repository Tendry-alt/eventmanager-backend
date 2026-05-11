package com.eventmanagement.backend.service

import com.eventmanagement.backend.model.VerificationToken
import com.eventmanagement.backend.repository.VerificationTokenRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class TokenService(
    private val verificationTokenRepository: VerificationTokenRepository
) {

    companion object {
        const val REGISTRATION = "REGISTRATION"
        const val PASSWORD_RESET = "PASSWORD_RESET"
        const val TOKEN_EXPIRY_HOURS = 24
        const val RESET_TOKEN_EXPIRY_HOURS = 1
    }

    fun createRegistrationToken(userId: Long): VerificationToken {
        return createToken(userId, REGISTRATION, TOKEN_EXPIRY_HOURS)
    }

    fun createPasswordResetToken(userId: Long): VerificationToken {
        return createToken(userId, PASSWORD_RESET, RESET_TOKEN_EXPIRY_HOURS)
    }

    private fun createToken(userId: Long, type: String, expiryHours: Int): VerificationToken {
        val token = VerificationToken(
            token = generateToken(),
            userId = userId,
            type = type,
            expiryDate = LocalDateTime.now().plusHours(expiryHours.toLong()),
            used = false,
            createdAt = LocalDateTime.now()
        )
        return verificationTokenRepository.save(token)
    }

    fun validateToken(token: String, type: String): VerificationToken? {
        val verificationToken = verificationTokenRepository.findByTokenAndType(token, type)

        return if (verificationToken != null &&
            !verificationToken.used &&
            verificationToken.expiryDate.isAfter(LocalDateTime.now())) {
            verificationToken
        } else {
            null
        }
    }

    fun markTokenAsUsed(token: VerificationToken) {
        token.used = true
        verificationTokenRepository.save(token)
    }

    private fun generateToken(): String {
        return UUID.randomUUID().toString()
    }
}