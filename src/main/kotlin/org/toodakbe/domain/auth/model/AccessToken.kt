package org.toodakbe.domain.auth.model

import java.time.Instant

/**
 * 발급된 JWT Access Token.
 *
 * @property value JWT 직렬화 결과 (`header.payload.signature`)
 * @property expiresAt 만료 시각 (`exp` claim에 해당)
 */
class AccessToken private constructor(
    val value: String,
    val expiresAt: Instant,
) {
    companion object {
        fun of(
            value: String,
            expiresAt: Instant,
        ): AccessToken {
            return AccessToken(value = value, expiresAt = expiresAt)
        }
    }
}
