package com.eventmanagement.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank(message = "Le token est requis")
    val token: String,

    @field:NotBlank(message = "Le nouveau mot de passe est requis")
    @field:Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    val newPassword: String
)