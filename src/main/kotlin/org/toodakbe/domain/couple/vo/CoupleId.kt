package org.toodakbe.domain.couple.vo

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

/**
 * 커플의 시스템 내부 식별자. UUID v7.
 */
@JvmInline
value class CoupleId(
    val value: UUID,
) {
    companion object {
        fun generate(): CoupleId = CoupleId(UuidCreator.getTimeOrderedEpoch())

        fun from(value: UUID): CoupleId = CoupleId(value)
    }
}
