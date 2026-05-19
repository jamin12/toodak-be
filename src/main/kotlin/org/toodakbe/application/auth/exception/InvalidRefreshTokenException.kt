package org.toodakbe.application.auth.exception

import org.toodakbe.core.exception.BaseRuntimeException

/**
 * Refresh Token 검증 실패 — 토큰 미존재(해시 미일치) 등 식별 불가 케이스.
 *
 * 만료/디바이스 불일치/재사용 감지는 각각 [RefreshTokenExpiredException],
 * [RefreshTokenDeviceMismatchException], [RefreshTokenReuseDetectedException] 로 구분한다.
 */
class InvalidRefreshTokenException(
    detail: String? = null,
) : BaseRuntimeException(AuthResponseCode.INVALID_REFRESH_TOKEN, detail)
