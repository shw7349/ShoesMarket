package com.shoesmarket.commerce

import com.shoesmarket.infra.persistence.MemberEntity
import com.shoesmarket.infra.persistence.MemberRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

class EmailAlreadyUsedException(
    email: String,
) : RuntimeException("이미 사용 중인 이메일입니다: $email")

class LoginFailedException : RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.")

data class LoggedInMember(
    val id: Long,
    val displayName: String,
)

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    @Transactional
    fun signUp(
        email: String,
        password: String,
        displayName: String,
    ): LoggedInMember {
        if (memberRepository.existsByEmail(email)) {
            throw EmailAlreadyUsedException(email)
        }
        val saved =
            memberRepository.save(
                MemberEntity(email = email, password = passwordEncoder.encode(password), displayName = displayName),
            )
        return LoggedInMember(saved.id, saved.displayName)
    }

    @Transactional(readOnly = true)
    fun login(
        email: String,
        password: String,
    ): LoggedInMember {
        val member = memberRepository.findByEmail(email) ?: throw LoginFailedException()
        if (!passwordEncoder.matches(password, member.password)) {
            throw LoginFailedException()
        }
        return LoggedInMember(member.id, member.displayName)
    }
}
