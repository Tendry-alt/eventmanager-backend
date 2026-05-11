package com.eventmanagement.backend.controller

import com.eventmanagement.backend.repository.UserRepository
import com.eventmanagement.backend.service.HashService
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class AuthWebController(
    private val userRepository: UserRepository,
    private val hashService: HashService
) {

    @GetMapping("/login")
    fun loginForm(): String {
        return "login"
    }

    @PostMapping("/login")
    fun login(
        @RequestParam email: String,
        @RequestParam password: String,
        session: HttpSession,
        model: Model
    ): String {
        val user = userRepository.findByEmail(email)

        if (user == null || !hashService.verifyPassword(password, user.passwordHash)) {
            model.addAttribute("error", true)
            return "login"
        }

        if (user.role != "admin") {
            model.addAttribute("error", true)
            return "login"
        }

        session.setAttribute("adminEmail", user.email)
        session.setAttribute("adminRole", user.role)

        return "redirect:/admin"
    }

    @GetMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/login"
    }

    // =====================================================
    // PAGES PUBLIQUES
    // =====================================================

    @GetMapping("/register")
    fun registerForm(): String {
        return "register"
    }

    @GetMapping("/admin-login")
    fun adminLoginForm(): String {
        return "login"
    }

    @GetMapping("/user-login")
    fun userLoginForm(): String {
        return "login-user"
    }

}