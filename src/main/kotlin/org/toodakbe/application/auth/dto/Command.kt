package org.toodakbe.application.auth.dto

/**
 * Google ID Token Flow 로그인 Command.
 *
 * @property idToken 모바일 클라이언트가 받아온 Google ID Token (JWT)
 * @property deviceId 클라이언트가 발급한 디바이스 식별자 (UUID). 디바이스 단위 세션 관리에 사용.
 * @property deviceLabel 사용자/관리자가 인식할 수 있는 디바이스 라벨. 선택.
 */
data class LoginWithGoogleCommand(
    val idToken: String,
    val deviceId: String,
    val deviceLabel: String?,
)
