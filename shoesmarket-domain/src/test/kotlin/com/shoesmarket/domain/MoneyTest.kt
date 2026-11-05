package com.shoesmarket.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MoneyTest :
    DescribeSpec({
        describe("Money 생성") {
            it("0 이상이면 생성된다") {
                Money(0).amount shouldBe 0
                Money(10_000).amount shouldBe 10_000
            }

            it("음수면 예외를 던진다") {
                shouldThrow<IllegalArgumentException> { Money(-1) }
            }
        }

        describe("Money 연산") {
            it("덧셈") {
                (Money(1_000) + Money(500)) shouldBe Money(1_500)
            }

            it("수량 곱셈") {
                (Money(1_000) * 3) shouldBe Money(3_000)
            }
        }
    })
