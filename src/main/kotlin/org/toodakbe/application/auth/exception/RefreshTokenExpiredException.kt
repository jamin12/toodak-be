package org.toodakbe.application.auth.exception

import org.toodakbe.core.exception.BaseRuntimeException

/**
 * Refresh Token 이 만료되었을 때 발생.
 */
class RefreshTokenExpiredException(
    detail: String? = null,
) : BaseRuntimeException(AuthResponseCode.REFRESH_EXPIRED, detail)
