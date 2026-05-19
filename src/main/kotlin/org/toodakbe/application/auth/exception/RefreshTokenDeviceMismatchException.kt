package org.toodakbe.application.auth.exception

import org.toodakbe.core.exception.BaseRuntimeException

/**
 * Refresh Token 의 발급 디바이스와 요청 디바이스가 일치하지 않을 때 발생.
 */
class RefreshTokenDeviceMismatchException(
    detail: String? = null,
) : BaseRuntimeException(AuthResponseCode.DEVICE_MISMATCH, detail)
