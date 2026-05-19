package org.toodakbe.application.auth.exception

import org.toodakbe.core.exception.BaseRuntimeException

/**
 * 이미 폐기(revoke)된 Refresh Token 으로 갱신 요청이 들어왔을 때 발생.
 *
 * 회전(rotate) 정책상 폐기된 토큰은 두 번 사용될 수 없다 — 발생하면 탈취로 간주하고
 * 해당 회원의 모든 활성 Refresh Token 을 일괄 회수한 뒤 이 예외를 던진다.
 */
class RefreshTokenReuseDetectedException(
    detail: String? = null,
) : BaseRuntimeException(AuthResponseCode.TOKEN_REUSE_DETECTED, detail)
