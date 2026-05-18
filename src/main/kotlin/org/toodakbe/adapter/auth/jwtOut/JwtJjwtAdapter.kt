package org.toodakbe.adapter.auth.jwtOut

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import org.toodakbe.application.auth.dto.AccessTokenClaims
import org.toodakbe.application.auth.exception.InvalidAccessTokenException
import org.toodakbe.application.auth.port.outbound.JwtOutPort
import org.toodakbe.domain.auth.model.AccessToken
import org.toodakbe.domain.member.vo.MemberId
import java.time.Clock
import java.time.Duration
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import io.jsonwebtoken.Clock as JjwtClock

/**
 * [JwtOutPort]의 JJWT 구현.
 *
 * HMAC-SHA256 대칭키 서명으로 Access Token을 발급/검증한다.
 * 시크릿은 [JwtProperties.secret]에서 주입되며 최소 256비트(32바이트) 이상이어야 한다.
 */
@Component
class JwtJjwtAdapter(
    properties: JwtProperties,
    private val clock: Clock,
) : JwtOutPort {
    private val key: SecretKey = Keys.hmacShaKeyFor(properties.secret.toByteArray(Charsets.UTF_8))

    /**
     * JJWT는 만료 검증 시 자체 [JjwtClock]을 사용한다 (기본은 시스템 시간).
     * 테스트의 결정론적 시간 제어를 위해 주입된 [clock]을 JJWT가 사용하도록 어댑팅한다.
     */
    private val jjwtClock = JjwtClock { Date.from(clock.instant()) }

    override fun issueAccessToken(
        memberId: MemberId,
        ttl: Duration,
    ): AccessToken {
        val now = clock.instant()
        val expiresAt = now.plus(ttl)
        val jwt =
            Jwts
                .builder()
                .subject(memberId.value.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key, Jwts.SIG.HS256)
                .compact()
        return AccessToken.of(value = jwt, expiresAt = expiresAt)
    }

    override fun parseAccessToken(token: String): AccessTokenClaims {
        val claims =
            try {
                Jwts
                    .parser()
                    .verifyWith(key)
                    .clock(jjwtClock)
                    .build()
                    .parseSignedClaims(token)
                    .payload
            } catch (e: JwtException) {
                throw InvalidAccessTokenException(cause = e)
            } catch (e: IllegalArgumentException) {
                throw InvalidAccessTokenException(cause = e)
            }

        val subject = claims.subject ?: throw InvalidAccessTokenException("Access Token에 subject가 없습니다.")
        val memberId =
            try {
                MemberId.from(UUID.fromString(subject))
            } catch (e: IllegalArgumentException) {
                throw InvalidAccessTokenException("Access Token의 subject가 UUID 형식이 아닙니다.", e)
            }
        val expiresAt =
            claims.expiration?.toInstant()
                ?: throw InvalidAccessTokenException("Access Token에 만료 시각이 없습니다.")

        return AccessTokenClaims(memberId = memberId, expiresAt = expiresAt)
    }
}
