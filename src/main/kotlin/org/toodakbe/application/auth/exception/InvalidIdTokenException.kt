package org.toodakbe.application.auth.exception

import org.toodakbe.core.exception.BaseRuntimeException

/**
 * Google ID Token 검증 실패 — 서명 불일치/만료/aud 불일치/iss 불일치/형식 오류 등.
 */
class InvalidIdTokenException(
    detail: String? = null,
    cause: Throwable? = null,
) : BaseRuntimeException(AuthResponseCode.INVALID_ID_TOKEN, detail, cause)
