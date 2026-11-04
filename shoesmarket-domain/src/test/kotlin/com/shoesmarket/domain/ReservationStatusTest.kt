package com.shoesmarket.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ReservationStatusTest :
    DescribeSpec({
        describe("ReservationStatus 상태 전이") {
            it("RESERVED는 CONFIRMED/EXPIRED/CANCELLED로 전이 가능") {
                ReservationStatus.RESERVED.canTransitTo(ReservationStatus.CONFIRMED) shouldBe true
                ReservationStatus.RESERVED.canTransitTo(ReservationStatus.EXPIRED) shouldBe true
                ReservationStatus.RESERVED.canTransitTo(ReservationStatus.CANCELLED) shouldBe true
            }

            it("RESERVED는 자기 자신으로 전이 불가") {
                ReservationStatus.RESERVED.canTransitTo(ReservationStatus.RESERVED) shouldBe false
            }

            it("CONFIRMED는 CANCELLED로만 전이 가능") {
                ReservationStatus.CONFIRMED.canTransitTo(ReservationStatus.CANCELLED) shouldBe true
                ReservationStatus.CONFIRMED.canTransitTo(ReservationStatus.CONFIRMED) shouldBe false
                ReservationStatus.CONFIRMED.canTransitTo(ReservationStatus.RESERVED) shouldBe false
                ReservationStatus.CONFIRMED.canTransitTo(ReservationStatus.EXPIRED) shouldBe false
            }

            it("EXPIRED/CANCELLED는 어떤 상태로도 전이 불가(종료 상태)") {
                ReservationStatus.entries.forEach { next ->
                    ReservationStatus.EXPIRED.canTransitTo(next) shouldBe false
                    ReservationStatus.CANCELLED.canTransitTo(next) shouldBe false
                }
            }
        }
    })
