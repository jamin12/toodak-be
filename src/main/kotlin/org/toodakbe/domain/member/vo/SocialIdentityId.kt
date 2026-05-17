package org.toodakbe.domain.member.vo

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

/**
 * SocialIdentity의 시스템 내부 식별자. UUID v7.
 *
 * 외부 노출 식별자는 `(provider, providerUserId)` 조합이지만, DB FK/내부 참조용으로
 * 별도 식별자를 둔다.
 */
@JvmInline
value class SocialIdentityId(
    val value: UUID,
) {
    companion object {
        fun generate(): SocialIdentityId = SocialIdentityId(UuidCreator.getTimeOrderedEpoch())

        fun from(value: UUID): SocialIdentityId = SocialIdentityId(value)
    }
}
