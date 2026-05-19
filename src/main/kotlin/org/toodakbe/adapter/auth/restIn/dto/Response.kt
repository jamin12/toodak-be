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
        description = "Refresh Token 평문. Phase 4 에서 채워지며 1차 스코프는 `null`.",
        example = "rt_xxxxxxxxxxxx",
        nullable = true,
    )
    val refreshToken: String?,
    @field:Schema(
        description = "Access Token 유효 기간 (초).",
        example = "900",
    )
    val expiresIn: Long,
)
