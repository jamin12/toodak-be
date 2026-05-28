package org.toodakbe.domain.couple.vo

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

/**
 * 페어링 초대의 시스템 내부 식별자. UUID v7.
 */
@JvmInline
value class InviteId(
    val value: UUID,
) {
    companion object {
        fun generate(): InviteId = InviteId(UuidCreator.getTimeOrderedEpoch())

        fun from(value: UUID): InviteId = InviteId(value)
    }
}
