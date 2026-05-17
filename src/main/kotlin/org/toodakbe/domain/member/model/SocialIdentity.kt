package org.toodakbe.domain.member.model

import org.toodakbe.domain.member.vo.MemberId
import org.toodakbe.domain.member.vo.Provider
import org.toodakbe.domain.member.vo.ProviderUserId
import org.toodakbe.domain.member.vo.SocialIdentityId
import java.time.Instant

/**
 * 한 Member가 가지고 있는 소셜 제공자 연결 정보.
 *
 * 도메인 불변식:
 * - `(provider, providerUserId)` 조합은 시스템 전체에서 unique
 * - `memberId`는 영구 불변 (다른 Member로 옮길 수 없음)
 * - `emailVerifiedAt`은 제공자가 이메일 검증을 보장해준 시각 — 자동 계정 연결 정책에 사용
 *
 * Member 1:N SocialIdentity. 한 사용자가 Google + Kakao 등 여러 소셜로 들어왔을 때
 * 같은 Member에 N개 연결될 수 있다.
 */
class SocialIdentity private constructor(
    val id: SocialIdentityId,
    val memberId: MemberId,
    val provider: Provider,
    val providerUserId: ProviderUserId,
    val emailVerifiedAt: Instant?,
    val linkedAt: Instant,
) {
    companion object {
        /**
         * Member에 새 소셜 제공자를 연결한다.
         *
         * @param emailVerifiedAt 제공자가 이메일 검증을 보장한 시각. 미검증이면 `null`.
         */
        fun link(
            memberId: MemberId,
            provider: Provider,
            providerUserId: ProviderUserId,
            emailVerifiedAt: Instant?,
            now: Instant,
        ): SocialIdentity =
            SocialIdentity(
                id = SocialIdentityId.generate(),
                memberId = memberId,
                provider = provider,
                providerUserId = providerUserId,
                emailVerifiedAt = emailVerifiedAt,
                linkedAt = now,
            )

        /**
         * 영속화 어댑터에서 도메인 모델을 복원할 때 사용.
         */
        fun restore(
            id: SocialIdentityId,
            memberId: MemberId,
            provider: Provider,
            providerUserId: ProviderUserId,
            emailVerifiedAt: Instant?,
            linkedAt: Instant,
        ): SocialIdentity =
            SocialIdentity(
                id = id,
                memberId = memberId,
                provider = provider,
                providerUserId = providerUserId,
                emailVerifiedAt = emailVerifiedAt,
                linkedAt = linkedAt,
            )
    }
}
