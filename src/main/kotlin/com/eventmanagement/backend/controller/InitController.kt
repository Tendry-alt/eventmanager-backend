package com.eventmanagement.backend.controller

import com.eventmanagement.backend.model.User
import com.eventmanagement.backend.repository.UserRepository
import com.eventmanagement.backend.service.HashService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/init")
@CrossOrigin(origins = ["*"])
class InitController(
    private val userRepository: UserRepository,
    private val hashService: HashService
) {

    @PostMapping("/hash-passwords")
    fun hashPasswords(): ResponseEntity<Map<String, Any>> {
        val users = userRepository.findAll()
        var updatedCount = 0

        users.forEach { user ->
            if (!user.passwordHash.startsWith("$2a")) {
                // Le mot de passe en clair est "password123" pour tous les comptes de test
                user.passwordHash = hashService.hashPassword("password123")
                userRepository.save(user)
                updatedCount++
            }
        }

        return ResponseEntity.ok(mapOf(
            "message" to "Mots de passe hachés avec succès",
            "updatedCount" to updatedCount
        ))
    }
}