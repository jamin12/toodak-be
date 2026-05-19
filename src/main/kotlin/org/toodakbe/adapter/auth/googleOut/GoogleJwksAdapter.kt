package org.toodakbe.adapter.auth.googleOut

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.toodakbe.application.auth.exception.InvalidIdTokenException
import org.toodakbe.application.auth.port.outbound.GoogleOAuthOutPort
import org.toodakbe.domain.auth.model.VerifiedSocialUser
import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.Provider
import org.toodakbe.domain.member.vo.ProviderUserId
import java.security.GeneralSecurityException

/**
 * [GoogleOAuthOutPort]의 Google API Client 구현.
 *
 * `GoogleIdTokenVerifier`는 내부에서 Google JWKS(`https://www.googleapis.com/oauth2/v3/certs`)를
 * 캐시하며 서명/aud/iss/exp 검증을 수행한다.
 */
@Component
@EnableConfigurationProperties(GoogleOAuthProperties::class)
class GoogleJwksAdapter(
    properties: GoogleOAuthProperties,
) : GoogleOAuthOutPort {
    private val verifier: GoogleIdTokenVerifier =
        GoogleIdTokenVerifier
            .Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(properties.audience))
            .build()

    override fun verifyIdToken(idToken: String): VerifiedSocialUser {
        val verified =
            try {
                verifier.verify(idToken)
            } catch (e: GeneralSecurityException) {
                throw InvalidIdTokenException("Google ID Token 서명/검증 실패", e)
            } catch (e: IllegalArgumentException) {
                throw InvalidIdTokenException("Google ID Token 형식이 올바르지 않습니다.", e)
            } catch (e: java.io.IOException) {
                throw InvalidIdTokenException("Google JWKS 조회 실패", e)
            } ?: throw InvalidIdTokenException("Google ID Token이 유효하지 않습니다.")

        val payload = verified.payload
        val sub = payload.subject ?: throw InvalidIdTokenException("Google ID Token에 sub 가 없습니다.")
        val email = payload.email ?: throw InvalidIdTokenException("Google ID Token에 email 이 없습니다.")
        val emailVerified = payload.emailVerified ?: false
        val name = payload["name"] as? String
        val picture = payload["picture"] as? String

        return VerifiedSocialUser.of(
            provider = Provider.GOOGLE,
            providerUserId = ProviderUserId(sub),
            email = Email(email),
            emailVerified = emailVerified,
            name = name,
            picture = picture,
        )
    }
}
