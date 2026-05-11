package com.eventmanagement.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest(
    @field:NotBlank(message = "L'email est requis")
    @field:Email(message = "Format email invalide")
    val email: String
)