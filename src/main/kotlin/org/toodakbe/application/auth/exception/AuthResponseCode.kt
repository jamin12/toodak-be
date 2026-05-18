package org.toodakbe.application.auth.exception

import org.toodakbe.core.response.ResponseCode

/**
 * auth 도메인 응답 코드. resultCode 채번은 12x 대 사용.
 */
enum class AuthResponseCode(
    override val httpStatus: Int,
    override val resultCode: Int,
    override val message: String,
) : ResponseCode {
    INVALID_TOKEN(401, 120, "Access Token이 유효하지 않습니다."),
    TOKEN_REUSE_DETECTED(401, 121, "재사용된 Refresh Token이 감지되었습니다."),
    REFRESH_EXPIRED(401, 122, "Refresh Token이 만료되었습니다."),
    DEVICE_MISMATCH(401, 123, "토큰 발급 디바이스와 요청 디바이스가 일치하지 않습니다."),
}
