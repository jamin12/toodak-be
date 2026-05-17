package org.toodakbe.domain.member.vo

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

/**
 * 회원의 시스템 내부 식별자.
 *
 * UUID v7을 사용해 시간 정렬이 가능하고 DB B-Tree 인덱스에 친화적이다.
 * 외부 노출에 안전(추측 불가)하며 영구 불변 식별자로 사용한다.
 */
@JvmInline
value class MemberId(
    val value: UUID,
) {
    companion object {
        fun generate(): MemberId = MemberId(UuidCreator.getTimeOrderedEpoch())

        fun from(value: UUID): MemberId = MemberId(value)
    }
}
