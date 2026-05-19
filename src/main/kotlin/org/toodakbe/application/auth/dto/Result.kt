package org.toodakbe.application.auth.dto

/**
 * Access/Refresh Token 발급 결과.
 *
 * @property accessToken 직렬화된 JWT Access Token
 * @property refreshToken 평문 Refresh Token. 발급/회전 시점에만 한 번 전달되며 DB 에는 해시만 저장된다.
 * @property expiresIn Access Token 유효 기간 (초)
 */
data class TokenPairResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)
