package org.toodakbe.domain.auth.vo

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

/**
 * RefreshToken row의 시스템 내부 식별자. UUID v7.
 *
 * 회전(rotate) 시 `rotatedFromId`로 이전 토큰을 참조하기 위해 도메인 식별자가 필요하다.
 */
@JvmInline
value class RefreshTokenId(
    val value: UUID,
) {
    companion object {
        fun generate(): RefreshTokenId = RefreshTokenId(UuidCreator.getTimeOrderedEpoch())

        fun from(value: UUID): RefreshTokenId = RefreshTokenId(value)
    }
}
