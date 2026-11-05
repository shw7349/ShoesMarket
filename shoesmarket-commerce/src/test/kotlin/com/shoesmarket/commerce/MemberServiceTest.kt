package com.shoesmarket.commerce

import com.shoesmarket.infra.persistence.MemberEntity
import com.shoesmarket.infra.persistence.MemberRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class MemberServiceTest :
    DescribeSpec({
        val memberRepository = mockk<MemberRepository>()
        val service = MemberService(memberRepository)

        beforeEach { clearMocks(memberRepository) }

        describe("회원가입") {
            it("이메일이 중복이면 예외를 던진다") {
                every { memberRepository.existsByEmail("a@b.com") } returns true
                shouldThrow<EmailAlreadyUsedException> { service.signUp("a@b.com", "pw", "철수") }
            }

            it("정상 가입하면 로그인 정보를 반환한다") {
                every { memberRepository.existsByEmail("a@b.com") } returns false
                every { memberRepository.save(any()) } answers { firstArg() }

                val result = service.signUp("a@b.com", "pw", "철수")
                result.displayName shouldBe "철수"
            }
        }

        describe("로그인") {
            it("비밀번호가 틀리면 예외를 던진다") {
                val encoded = BCryptPasswordEncoder().encode("right")
                every { memberRepository.findByEmail("a@b.com") } returns MemberEntity("a@b.com", encoded, "철수")

                shouldThrow<LoginFailedException> { service.login("a@b.com", "wrong") }
            }

            it("비밀번호가 맞으면 로그인된다") {
                val encoded = BCryptPasswordEncoder().encode("right")
                every { memberRepository.findByEmail("a@b.com") } returns MemberEntity("a@b.com", encoded, "철수")

                service.login("a@b.com", "right").displayName shouldBe "철수"
            }
        }
    })
