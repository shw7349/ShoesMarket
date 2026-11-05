package com.shoesmarket.api.web

import com.shoesmarket.infra.persistence.OrderRepository
import com.shoesmarket.infra.persistence.ReservationRepository
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class MyPageController(
    private val orderRepository: OrderRepository,
    private val reservationRepository: ReservationRepository,
    private val webSession: WebSession,
) {
    @GetMapping("/mypage")
    fun myPage(
        session: HttpSession,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        val member = webSession.current(session)
        if (member == null) {
            redirectAttributes.addFlashAttribute("toast", "로그인이 필요합니다.")
            return "redirect:/login"
        }
        model.addAttribute("orders", orderRepository.findByUserIdOrderByIdDesc(member.id))
        model.addAttribute("reservations", reservationRepository.findByUserIdOrderByIdDesc(member.id))
        return "mypage"
    }
}
