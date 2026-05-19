package org.toodakbe.application.auth.port.outbound

import org.toodakbe.domain.auth.model.VerifiedSocialUser

/**
 * Google OAuth ID Token 검증 OutPort.
 */
interface GoogleOAuthOutPort {
    /**
     * Google ID Token 을 검증하고 사용자 정보를 [VerifiedSocialUser] 로 반환한다.
     *
     * 검증 항목: 서명(JWKS), `aud`(client_id), `iss`(accounts.google.com), `exp`.
     *
     * @throws org.toodakbe.application.auth.exception.InvalidIdTokenException 검증 실패 시
     */
    fun verifyIdToken(idToken: String): VerifiedSocialUser
}
