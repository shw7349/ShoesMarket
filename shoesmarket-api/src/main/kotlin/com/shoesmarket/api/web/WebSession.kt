package com.shoesmarket.api.web

import com.shoesmarket.commerce.LoggedInMember
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Component

const val LOGIN_MEMBER = "LOGIN_MEMBER"

@Component
class WebSession {
    fun login(
        session: HttpSession,
        member: LoggedInMember,
    ) {
        session.setAttribute(LOGIN_MEMBER, member)
    }

    fun current(session: HttpSession): LoggedInMember? = session.getAttribute(LOGIN_MEMBER) as? LoggedInMember

    fun logout(session: HttpSession) {
        session.invalidate()
    }
}
