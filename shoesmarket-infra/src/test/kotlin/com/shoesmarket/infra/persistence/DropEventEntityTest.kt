package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.DropStatus
import com.shoesmarket.domain.InvalidStatusTransitionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class DropEventEntityTest :
    DescribeSpec({
        fun newDrop() =
            DropEventEntity(
                productId = 1L,
                totalStock = 100,
                perUserLimit = 1,
                openAt = Instant.parse("2026-11-20T11:00:00Z"),
                closeAt = Instant.parse("2026-11-20T12:00:00Z"),
            )

        describe("DropEvent 상태 전이") {
            it("SCHEDULED -> OPEN -> CLOSED") {
                val drop = newDrop()
                drop.status shouldBe DropStatus.SCHEDULED
                drop.open()
                drop.status shouldBe DropStatus.OPEN
                drop.close()
                drop.status shouldBe DropStatus.CLOSED
            }

            it("OPEN -> SOLD_OUT -> CLOSED") {
                val drop = newDrop()
                drop.open()
                drop.soldOut()
                drop.status shouldBe DropStatus.SOLD_OUT
                drop.close()
                drop.status shouldBe DropStatus.CLOSED
            }

            it("SCHEDULED에서 바로 close하면 예외") {
                val drop = newDrop()
                shouldThrow<InvalidStatusTransitionException> { drop.close() }
            }
        }
    })
