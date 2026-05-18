package org.toodakbe.application.auth.port.outbound

import org.toodakbe.application.auth.dto.AccessTokenClaims
import org.toodakbe.domain.auth.model.AccessToken
import org.toodakbe.domain.member.vo.MemberId
import java.time.Duration

/**
 * JWT 발급/검증 외부 의존(JJWT)을 추상화하는 OutPort.
 *
 * "한 외부 의존 = 한 OutPort" 규칙에 따라 JJWT 라이브러리 호출을 이 인터페이스 뒤로 격리한다.
 * UseCase는 [issueAccessToken]/[parseAccessToken] 두 메서드만 통해 JWT를 다룬다.
 */
interface JwtOutPort {
    /**
     * Access Token 발급.
     *
     * @param memberId 토큰 subject(`sub` claim)로 들어갈 회원 식별자
     * @param ttl 토큰 유효 기간 (`exp = now + ttl`)
     * @return 직렬화된 JWT와 만료 시각을 묶은 도메인 모델
     */
    fun issueAccessToken(
        memberId: MemberId,
        ttl: Duration,
    ): AccessToken

    /**
     * Access Token 검증 및 클레임 추출.
     *
     * 서명 검증, 만료 검증, subject 파싱을 모두 수행한다.
     * 어떤 사유로든 검증에 실패하면 [org.toodakbe.application.auth.exception.InvalidAccessTokenException]을 던진다.
     */
    fun parseAccessToken(token: String): AccessTokenClaims
}
