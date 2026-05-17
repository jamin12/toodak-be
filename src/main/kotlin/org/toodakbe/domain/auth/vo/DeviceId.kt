package org.toodakbe.domain.auth.vo

/**
 * 클라이언트 디바이스 식별자.
 *
 * 모바일 앱이 최초 실행 시 발급(UUID 생성)하여 안전한 저장소(Keychain/EncryptedSharedPreferences)에
 * 보관하고, 매 인증 요청에 `X-Device-Id` 헤더로 전송한다.
 *
 * RefreshToken은 디바이스 단위로 발급되며, 같은 디바이스에서 재로그인 시 기존 활성 토큰을 회수한다.
 */
@JvmInline
value class DeviceId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "deviceId는 비어 있을 수 없습니다" }
    }
}
