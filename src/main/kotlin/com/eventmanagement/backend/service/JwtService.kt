package com.eventmanagement.backend.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {

    private val secretKey: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private val expirationTime: Long = 3600000 // 1 heure en millisecondes

    fun generateToken(email: String, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)

        return Jwts.builder()
            .setSubject(email)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            extractClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun extractEmail(token: String): String {
        return extractClaims(token).subject
    }

    fun extractRole(token: String): String {
        return extractClaims(token).get("role", String::class.java)
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
}