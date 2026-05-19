package org.toodakbe.adapter.auth.restIn.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Google ID Token Flow 로그인 요청")
data class LoginWithGoogleRequest(
    @field:Schema(
        description = "Google SDK 가 발급한 ID Token (JWT). 서버가 Google JWKS 로 서명/aud/iss/exp 를 검증한다.",
        example = "eyJhbGciOiJSUzI1NiIs...",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:NotBlank(message = "idToken은 필수입니다.")
    val idToken: String,
    @field:Schema(
        description = "클라이언트가 발급한 디바이스 식별자 (UUID). 디바이스 단위 세션 관리에 사용된다.",
        example = "00000000-0000-0000-0000-000000000001",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:NotBlank(message = "deviceId는 필수입니다.")
    val deviceId: String,
    @field:Schema(
        description = "사용자/관리자가 인식할 수 있는 디바이스 라벨.",
        example = "iPhone 15 Pro",
        nullable = true,
    )
    val deviceLabel: String? = null,
)
