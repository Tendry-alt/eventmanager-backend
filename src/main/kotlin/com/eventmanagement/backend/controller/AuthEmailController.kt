package com.eventmanagement.backend.controller

import com.eventmanagement.backend.dto.*
import com.eventmanagement.backend.model.User
import com.eventmanagement.backend.repository.UserRepository
import com.eventmanagement.backend.repository.VerificationTokenRepository
import com.eventmanagement.backend.service.EmailService
import com.eventmanagement.backend.service.HashService
import com.eventmanagement.backend.service.JwtService
import com.eventmanagement.backend.service.TokenService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Controller
@RequestMapping("/api/auth")
class AuthEmailController(
    private val userRepository: UserRepository,
    private val hashService: HashService,
    private val emailService: EmailService,
    private val tokenService: TokenService,
    private val verificationTokenRepository: VerificationTokenRepository,
    private val jwtService: JwtService
) {

    @Value("\${app.base.url}")
    private lateinit var baseUrl: String

    // =====================================================
    // CONNEXION (API REST pour mobile)
    // =====================================================

    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Map<String, Any>> {
        val user = userRepository.findByEmail(request.email)

        if (user == null || !hashService.verifyPassword(request.password, user.passwordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Email ou mot de passe incorrect"))
        }

        if (!user.enabled) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Compte en attente de validation. Vérifiez votre email."))
        }

        val token = jwtService.generateToken(user.email, user.role)

        val response = mapOf(
            "token" to token,
            "email" to user.email,
            "role" to user.role,
            "userId" to (user.id ?: 0L)
        )

        return ResponseEntity.ok(response)
    }

    // =====================================================
    // INSCRIPTION (API REST pour mobile)
    // =====================================================

    @PostMapping("/register")
    @ResponseBody
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<Map<String, Any>> {

        if (userRepository.findByEmail(request.email) != null) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(mapOf("error" to "Cet email est déjà utilisé"))
        }

        val user = User().apply {
            email = request.email
            passwordHash = hashService.hashPassword(request.password)
            role = request.role
            enabled = false
            createdAt = LocalDateTime.now()
        }

        val savedUser = userRepository.save(user)

        val token = tokenService.createRegistrationToken(savedUser.id!!)

        // Utiliser l'IP configurée par l'utilisateur, sinon l'IP par défaut
        val confirmationBaseUrl = if (!request.serverIp.isNullOrBlank()) {
            "http://${request.serverIp}:8080"
        } else {
            baseUrl
        }

        emailService.sendVerificationEmail(request.email, token.token, confirmationBaseUrl)

        val response = mapOf(
            "message" to "Inscription réussie. Vérifiez votre email pour activer votre compte.",
            "userId" to (savedUser.id ?: 0L)
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // =====================================================
    // VALIDATION EMAIL (page HTML)
    // =====================================================

    @GetMapping("/verify")
    fun verifyEmail(@RequestParam token: String, model: Model): String {
        val verificationToken = tokenService.validateToken(token, TokenService.REGISTRATION)

        if (verificationToken == null) {
            model.addAttribute("error", true)
            model.addAttribute("message", "Lien de validation invalide ou expiré.")
            return "verification-result"
        }

        val user = userRepository.findById(verificationToken.userId).orElse(null)

        if (user == null) {
            model.addAttribute("error", true)
            model.addAttribute("message", "Utilisateur non trouvé.")
            return "verification-result"
        }

        // Ajouter l'email au modèle pour toutes les situations
        model.addAttribute("email", user.email)

        if (user.enabled) {
            model.addAttribute("pending", true)
            model.addAttribute("message", "Votre compte est déjà activé. Vous pouvez vous connecter à l'application mobile EventManager.")
            return "verification-result"
        }

        // Activer le compte
        user.enabled = true
        userRepository.save(user)
        tokenService.markTokenAsUsed(verificationToken)

        model.addAttribute("success", true)
        model.addAttribute("message", "Votre compte a été activé avec succès ! Connectez-vous à l'application mobile EventManager pour consulter les événements et obtenir vos tickets.")

        return "verification-result"
    }

    // =====================================================
    // MOT DE PASSE OUBLIÉ
    // =====================================================

    @PostMapping("/forgot-password")
    @ResponseBody
    fun forgotPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<Map<String, String>> {

        val user = userRepository.findByEmail(request.email)

        if (user == null) {
            return ResponseEntity.ok(mapOf("message" to "Si cet email existe, vous recevrez un lien de réinitialisation."))
        }

        val token = tokenService.createPasswordResetToken(user.id!!)

        emailService.sendPasswordResetEmail(request.email, token.token, baseUrl)

        return ResponseEntity.ok(mapOf("message" to "Si cet email existe, vous recevrez un lien de réinitialisation."))
    }

    // =====================================================
    // RÉINITIALISATION MOT DE PASSE
    // =====================================================

    @PostMapping("/reset-password")
    @ResponseBody
    fun resetPassword(@Valid @RequestBody request: ChangePasswordRequest): ResponseEntity<Map<String, String>> {

        val verificationToken = tokenService.validateToken(request.token, TokenService.PASSWORD_RESET)

        if (verificationToken == null) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "Lien de réinitialisation invalide ou expiré"))
        }

        val user = userRepository.findById(verificationToken.userId).orElse(null)

        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Utilisateur non trouvé"))
        }

        user.passwordHash = hashService.hashPassword(request.newPassword)
        userRepository.save(user)

        tokenService.markTokenAsUsed(verificationToken)

        return ResponseEntity.ok(mapOf("message" to "Mot de passe réinitialisé avec succès"))
    }
}