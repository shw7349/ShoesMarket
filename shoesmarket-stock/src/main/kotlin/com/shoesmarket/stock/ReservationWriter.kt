package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReservationId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.ReservationEntity
import com.shoesmarket.infra.persistence.ReservationRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import java.time.Clock

/**
 * 선점 레코드를 저장한다. 1인 제한은 reservation(drop_id,user_id) 유니크 제약이 최후 방어선이다.
 * 비교용 DB 락 전략들이 공유한다.
 */
@Component
class ReservationWriter(
    private val reservationRepository: ReservationRepository,
    private val reservationProperties: ReservationProperties,
    private val clock: Clock,
) {
    fun write(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult {
        val expiresAt = clock.instant().plusSeconds(reservationProperties.ttlSeconds)
        return try {
            val saved =
                reservationRepository.save(
                    ReservationEntity(dropId = dropId.value, userId = userId.value, expiresAt = expiresAt),
                )
            ReserveResult.Success(ReservationId(saved.id), expiresAt)
        } catch (e: DataIntegrityViolationException) {
            ReserveResult.AlreadyPurchased
        }
    }

    fun alreadyReserved(
        dropId: DropId,
        userId: UserId,
    ): Boolean = reservationRepository.findByDropIdAndUserId(dropId.value, userId.value) != null
}
