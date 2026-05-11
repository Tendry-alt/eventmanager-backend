package com.eventmanagement.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "Format email invalide")
    val email: String,

    @field:NotBlank(message = "Le mot de passe est requis")
    @field:Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    val password: String,

    val role: String = "user",

    val serverIp: String? = null
)