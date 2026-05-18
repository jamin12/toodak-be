package org.toodakbe.application.auth.exception

import org.toodakbe.core.exception.BaseRuntimeException

/**
 * Access Token 검증 실패 — 서명 불일치/만료/형식 오류/주체 파싱 실패 등.
 *
 * 인증 필터가 잡으면 SecurityContext만 비우고, 이후 `AuthEntryPoint`가 401 응답을 생성한다.
 * 직접 전파되면 `GlobalExceptionHandler`가 [AuthResponseCode.INVALID_TOKEN]으로 응답한다.
 */
class InvalidAccessTokenException(
    detail: String? = null,
    cause: Throwable? = null,
) : BaseRuntimeException(AuthResponseCode.INVALID_TOKEN, detail, cause)
