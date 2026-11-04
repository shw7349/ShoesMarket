package com.shoesmarket.api.common

import com.shoesmarket.domain.UserId
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * 컨트롤러 파라미터 타입이 [UserId]면 요청 헤더 `X-User-Id`를 변환해 주입한다 (SPEC 6.1 간이 인증).
 * 헤더가 없거나 숫자가 아니면 UNAUTHORIZED.
 */
@Component
class LoginUserIdArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == UserId::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): UserId {
        val raw = webRequest.getHeader(USER_ID_HEADER) ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        val value = raw.toLongOrNull() ?: throw ApiException(ErrorCode.UNAUTHORIZED, "X-User-Id 헤더가 올바르지 않습니다.")
        return UserId(value)
    }

    companion object {
        const val USER_ID_HEADER = "X-User-Id"
    }
}
