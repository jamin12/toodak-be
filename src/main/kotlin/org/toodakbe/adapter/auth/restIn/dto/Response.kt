package org.toodakbe.adapter.auth.restIn.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Access/Refresh Token 발급 결과")
data class TokenPairResponse(
    @field:Schema(
        description = "JWT Access Token. 보호된 API 호출 시 `Authorization: Bearer <accessToken>` 헤더로 전달한다.",
        example = "eyJhbGciOiJIUzI1NiJ9...",
    )
    val accessToken: String,
    @field:Schema(
        description = "평문 Refresh Token. 발급/회전 시점에만 한 번 전달되며 안전한 저장소(Keychain 등)에 보관한다.",
        example = "rt_xxxxxxxxxxxx",
    )
    val refreshToken: String,
    @field:Schema(
        description = "Access Token 유효 기간 (초).",
        example = "900",
    )
    val expiresIn: Long,
)
