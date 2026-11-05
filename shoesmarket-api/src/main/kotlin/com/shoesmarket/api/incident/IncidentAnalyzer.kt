package com.shoesmarket.api.incident

import com.shoesmarket.domain.IncidentCategory
import com.shoesmarket.domain.InvalidStatusTransitionException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.RedisSystemException
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import java.io.IOException

data class IncidentAnalysis(
    val category: IncidentCategory,
    val probableCause: String,
    val suggestedAction: String,
)

/**
 * 규칙 기반 장애 원인 분석기. 예외 유형을 카테고리·추정 원인·권장 조치로 변환한다.
 */
@Component
class IncidentAnalyzer {
    fun analyze(throwable: Throwable): IncidentAnalysis {
        // 네트워크/외부 I/O 오류는 다른 예외에 감싸여 전파되는 경우가 많아 cause 체인까지 확인한다.
        if (isNetworkError(throwable)) {
            return IncidentAnalysis(
                category = IncidentCategory.NETWORK,
                probableCause = "네트워크/외부 연동 I/O 실패(연결 거부·타임아웃·호스트 미해결 등). PG 등 외부 호출이 영향받는다.",
                suggestedAction = "대상 서비스 상태와 타임아웃/재시도 설정을 확인하고, 결제 경로는 대사 배치로 복구 가능한지 점검한다.",
            )
        }
        return when (throwable) {
            is DataIntegrityViolationException ->
                IncidentAnalysis(
                    category = IncidentCategory.DATA_INTEGRITY,
                    probableCause = "DB 제약 위반. 동일 사용자 중복 선점/주문 또는 유니크 키 충돌 가능성이 높다.",
                    suggestedAction = "멱등 처리와 보상 로직(Redis release)을 확인하고, reservation(drop_id,user_id) 유니크 제약이 유지되는지 점검한다.",
                )

            is RedisConnectionFailureException ->
                IncidentAnalysis(
                    category = IncidentCategory.REDIS_FAILURE,
                    probableCause = "Redis 연결 실패. 대기열/재고 선점 경로가 동작하지 않는다.",
                    suggestedAction = "Redis 기동 상태와 네트워크를 확인하고, 복구 후 DB 기준으로 재고(drop:{id}:stock)를 재초기화한다.",
                )

            is RedisSystemException ->
                IncidentAnalysis(
                    category = IncidentCategory.REDIS_FAILURE,
                    probableCause = "Redis 명령 실행 오류(Lua 스크립트 오류 또는 타입 불일치 가능).",
                    suggestedAction = "reserve/release Lua 스크립트와 키 타입(stock=String, buyers=Set)을 점검한다.",
                )

            is InvalidStatusTransitionException ->
                IncidentAnalysis(
                    category = IncidentCategory.STATUS_TRANSITION,
                    probableCause = "허용되지 않은 상태 전이 시도. 만료와 결제 확정의 경합 등으로 상태가 이미 바뀌었을 수 있다.",
                    suggestedAction = "조건부 UPDATE로 한쪽만 성공하는지 확인하고, 호출 순서를 점검한다.",
                )

            else ->
                IncidentAnalysis(
                    category = IncidentCategory.UNKNOWN,
                    probableCause = "미분류 예외(${throwable.javaClass.simpleName}). 원인 추가 분석이 필요하다.",
                    suggestedAction = "스택트레이스와 컨텍스트(dropId/userId)를 확인하고 분석 규칙에 유형을 추가한다.",
                )
        }
    }

    private fun isNetworkError(throwable: Throwable): Boolean {
        var cause: Throwable? = throwable
        val seen = mutableSetOf<Throwable>()
        while (cause != null && seen.add(cause)) {
            if (cause is IOException || cause is ResourceAccessException) {
                return true
            }
            cause = cause.cause
        }
        return false
    }
}
