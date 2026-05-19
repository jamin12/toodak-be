package org.toodakbe.domain.auth.model

import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.Provider
import org.toodakbe.domain.member.vo.ProviderUserId

/**
 * 외부 소셜 제공자(Google/Kakao/Apple …)가 검증해준 사용자 정보.
 *
 * 각 제공자별 `*OAuthOutPort` 가 자기 외부 의존을 검증한 뒤 이 타입으로 통일된 결과를 반환한다.
 * UseCase 는 [provider] 필드로 분기만 알아도 충분하며, 제공자별 도메인 모델을 따로 만들지 않는다.
 *
 * @property emailVerified 제공자가 사용자 이메일 소유를 검증했는지 여부.
 *   자동 계정 연결 정책의 핵심 판단 기준 ([Provider.trustsEmailVerification] 와 결합).
 */
class VerifiedSocialUser private constructor(
    val provider: Provider,
    val providerUserId: ProviderUserId,
    val email: Email,
    val emailVerified: Boolean,
    val name: String?,
    val picture: String?,
) {
    companion object {
        fun of(
            provider: Provider,
            providerUserId: ProviderUserId,
            email: Email,
            emailVerified: Boolean,
            name: String? = null,
            picture: String? = null,
        ): VerifiedSocialUser =
            VerifiedSocialUser(
                provider = provider,
                providerUserId = providerUserId,
                email = email,
                emailVerified = emailVerified,
                name = name,
                picture = picture,
            )
    }
}
