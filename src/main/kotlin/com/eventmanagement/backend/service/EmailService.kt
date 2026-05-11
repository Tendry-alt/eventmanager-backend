package com.eventmanagement.backend.service

import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {

    @Throws(MessagingException::class)
    fun sendVerificationEmail(to: String, token: String, baseUrl: String) {
        val subject = "Confirmez votre inscription - EventManager"
        val confirmationUrl = "$baseUrl/api/auth/verify?token=$token"
        val content = buildEmailContent(
            title = "Vérification de votre compte",
            message = "Cliquez sur le lien ci-dessous pour activer votre compte :",
            buttonUrl = confirmationUrl,
            footer = "Ce lien expirera dans 24 heures.",
            showNetworkWarning = true  // ← Afficher l'avertissement réseau
        )

        sendHtmlEmail(to, subject, content)
    }

    @Throws(MessagingException::class)
    fun sendPasswordResetEmail(to: String, token: String, baseUrl: String) {
        val subject = "Réinitialisation de votre mot de passe - EventManager"
        val resetUrl = "$baseUrl/auth/reset-password-form?token=$token"
        val content = buildEmailContent(
            title = "Réinitialisation du mot de passe",
            message = "Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :",
            buttonUrl = resetUrl,
            footer = "Ce lien expirera dans 1 heure.",
            showNetworkWarning = false  // ← Pas d'avertissement pour réinitialisation
        )

        sendHtmlEmail(to, subject, content)
    }

    @Throws(MessagingException::class)
    fun sendTicketEmail(to: String, eventName: String, ticketId: Long, qrCodeBase64: String) {
        val subject = "Votre ticket pour $eventName - EventManager"
        val content = buildTicketEmailContent(eventName, ticketId, qrCodeBase64)

        sendHtmlEmail(to, subject, content)
    }

    // =====================================================
    // MÉTHODES POUR VALIDATION ADMIN
    // =====================================================

    @Throws(MessagingException::class)
    fun sendNewRegistrationNotification(adminEmail: String, userEmail: String, userId: Long) {
        val subject = "Nouvelle inscription en attente de validation"
        val adminUrl = "http://localhost:8080/admin/users"
        val content = buildEmailContent(
            title = "Nouvelle inscription",
            message = "L'utilisateur $userEmail vient de s'inscrire et est en attente de validation.",
            buttonUrl = adminUrl,
            footer = "Connectez-vous à l'interface admin pour valider ou refuser cette inscription.",
            showNetworkWarning = false
        )
        sendHtmlEmail(adminEmail, subject, content)
    }

    @Throws(MessagingException::class)
    fun sendAccountActivatedEmail(userEmail: String, baseUrl: String) {
        val subject = "Votre compte a été activé - EventManager"
        val loginUrl = "$baseUrl/user-login"
        val content = buildEmailContent(
            title = "Compte activé",
            message = "Votre compte a été validé par l'administrateur.",
            buttonUrl = loginUrl,
            footer = "Vous pouvez maintenant vous connecter à l'application EventManager.",
            showNetworkWarning = true  // ← Avertissement réseau
        )
        sendHtmlEmail(userEmail, subject, content)
    }

    @Throws(MessagingException::class)
    fun sendAccountRejectedEmail(userEmail: String) {
        val subject = "Votre inscription - EventManager"
        val content = buildSimpleEmailContent(
            title = "Inscription non validée",
            message = "Nous vous remercions pour votre inscription. Malheureusement, votre compte n'a pas été validé par l'administrateur.",
            footer = "Si vous avez des questions, veuillez contacter l'administrateur."
        )
        sendHtmlEmail(userEmail, subject, content)
    }

    // =====================================================
    // MÉTHODES PRIVÉES
    // =====================================================

    private fun buildEmailContent(
        title: String,
        message: String,
        buttonUrl: String,
        footer: String,
        showNetworkWarning: Boolean = false
    ): String {
        val context = Context().apply {
            setVariable("title", title)
            setVariable("message", message)
            setVariable("buttonUrl", buttonUrl)
            setVariable("footer", footer)
            setVariable("showNetworkWarning", showNetworkWarning)
        }
        return templateEngine.process("email/verification", context)
    }

    private fun buildTicketEmailContent(eventName: String, ticketId: Long, qrCodeBase64: String): String {
        val context = Context().apply {
            setVariable("eventName", eventName)
            setVariable("ticketId", ticketId)
            setVariable("qrCode", qrCodeBase64)
        }
        return templateEngine.process("email/ticket", context)
    }

    private fun buildSimpleEmailContent(title: String, message: String, footer: String): String {
        val context = Context().apply {
            setVariable("title", title)
            setVariable("message", message)
            setVariable("buttonUrl", "#")
            setVariable("footer", footer)
            setVariable("showNetworkWarning", false)
        }
        return templateEngine.process("email/verification", context)
    }

    @Throws(MessagingException::class)
    private fun sendHtmlEmail(to: String, subject: String, htmlContent: String) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlContent, true)
        helper.setFrom("noreply@eventmanager.com")

        mailSender.send(message)
    }
}