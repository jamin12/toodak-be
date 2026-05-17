package org.toodakbe.domain.auth.model

/**
 * Google ID Token 검증을 통해 얻은 사용자 정보.
 *
 * `GoogleOAuthOutPort.verifyIdToken`이 Google JWKS로 서명/aud/iss/exp 검증을 수행한 결과를
 * 도메인 모델로 표현한다. UseCase는 이 객체만 의존하며 Google 통신/JWT 라이브러리에 무지하다.
 *
 * @property sub Google 영구 사용자 식별자. SocialIdentity.providerUserId로 사용.
 * @property email 사용자 이메일.
 * @property emailVerified Google이 이메일 소유를 검증했는지 여부.
 *   자동 계정 연결 정책의 핵심 판단 기준.
 */
class GoogleUser private constructor(
    val sub: String,
    val email: String,
    val emailVerified: Boolean,
    val name: String?,
    val picture: String?,
) {
    companion object {
        fun of(
            sub: String,
            email: String,
            emailVerified: Boolean,
            name: String? = null,
            picture: String? = null,
        ): GoogleUser {
            return GoogleUser(
                sub = sub,
                email = email,
                emailVerified = emailVerified,
                name = name,
                picture = picture,
            )
        }
    }
}
