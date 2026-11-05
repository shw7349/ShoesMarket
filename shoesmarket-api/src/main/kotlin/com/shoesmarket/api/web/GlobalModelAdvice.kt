package com.shoesmarket.api.web

import com.shoesmarket.commerce.LoggedInMember
import jakarta.servlet.http.HttpSession
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * 모든 뷰에 로그인 회원 정보를 넣어 헤더에서 로그인 상태를 보여준다.
 */
@ControllerAdvice(basePackages = ["com.shoesmarket.api.web"])
class GlobalModelAdvice(
    private val webSession: WebSession,
) {
    @ModelAttribute("loginMember")
    fun loginMember(session: HttpSession): LoggedInMember? = webSession.current(session)
}
