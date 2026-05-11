package com.eventmanagement.backend.service

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class HashService {

    private val encoder = BCryptPasswordEncoder()

    fun hashPassword(password: String): String {
        return encoder.encode(password)
    }

    fun verifyPassword(rawPassword: String, hashedPassword: String): Boolean {
        return encoder.matches(rawPassword, hashedPassword)
    }
}