package org.toodakbe.application.auth.dto

/**
 * Access/Refresh Token 발급 결과.
 *
 * @property accessToken 직렬화된 JWT Access Token
 * @property refreshToken Refresh Token 평문. Phase 4 에서 채워진다 (1차 스코프는 `null`).
 * @property expiresIn Access Token 유효 기간 (초)
 */
data class TokenPairResult(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long,
)
