package com.shoesmarket.api.web

import com.shoesmarket.commerce.EmailAlreadyUsedException
import com.shoesmarket.commerce.LoginFailedException
import com.shoesmarket.commerce.MemberService
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AuthWebController(
    private val memberService: MemberService,
    private val webSession: WebSession,
) {
    @GetMapping("/login")
    fun loginForm(): String = "login"

    @PostMapping("/login")
    fun login(
        @RequestParam email: String,
        @RequestParam password: String,
        session: HttpSession,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            webSession.login(session, memberService.login(email, password))
            redirectAttributes.addFlashAttribute("toast", "로그인되었습니다.")
            "redirect:/"
        } catch (e: LoginFailedException) {
            model.addAttribute("error", e.message)
            model.addAttribute("email", email)
            "login"
        }

    @GetMapping("/signup")
    fun signUpForm(): String = "signup"

    @PostMapping("/signup")
    fun signUp(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam displayName: String,
        session: HttpSession,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String =
        try {
            webSession.login(session, memberService.signUp(email, password, displayName))
            redirectAttributes.addFlashAttribute("toast", "가입을 환영합니다, $displayName 님!")
            "redirect:/"
        } catch (e: EmailAlreadyUsedException) {
            model.addAttribute("error", e.message)
            model.addAttribute("email", email)
            model.addAttribute("displayName", displayName)
            "signup"
        }

    @PostMapping("/logout")
    fun logout(
        session: HttpSession,
        redirectAttributes: RedirectAttributes,
    ): String {
        webSession.logout(session)
        redirectAttributes.addFlashAttribute("toast", "로그아웃되었습니다.")
        return "redirect:/"
    }
}
