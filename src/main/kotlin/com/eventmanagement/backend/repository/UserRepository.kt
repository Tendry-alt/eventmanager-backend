package com.eventmanagement.backend.repository

import com.eventmanagement.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    // Rechercher un utilisateur par son email (exact)
    fun findByEmail(email: String): User?

    // Rechercher les utilisateurs par email (contient, insensible à la casse)
    fun findByEmailContainingIgnoreCase(email: String): List<User>

    // Rechercher les utilisateurs par rôle
    fun findByRole(role: String): List<User>

    // Rechercher les utilisateurs par rôle (insensible à la casse)
    fun findByRoleIgnoreCase(role: String): List<User>

    // Vérifier si un email existe déjà
    fun existsByEmail(email: String): Boolean

    // Compter les utilisateurs par rôle
    fun countByRole(role: String): Long
}