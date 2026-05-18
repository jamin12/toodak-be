package org.toodakbe.application.auth.service

import org.springframework.stereotype.Service
import org.toodakbe.application.auth.dto.AccessTokenClaims
import org.toodakbe.application.auth.port.inbound.VerifyAccessTokenInPort
import org.toodakbe.application.auth.port.outbound.JwtOutPort

/**
 * Access Token 검증 UseCase.
 *
 * 현재는 [JwtOutPort.parseAccessToken]에 단순 위임하지만, 인증 정책이 application 레이어에 모이도록
 * driving adapter(필터)가 OutPort를 직접 의존하는 것을 차단한다.
 *
 * 향후 확장 자리:
 * - WITHDRAWN 회원의 토큰 거부 (MemberOutPort 조회 추가)
 * - 토큰 블랙리스트 검증
 * - 비밀번호/시크릿 회전 시각 이후 토큰만 허용
 */
@Service
class VerifyAccessTokenUseCase(
    private val jwtOutPort: JwtOutPort,
) : VerifyAccessTokenInPort {
    override fun verify(accessToken: String): AccessTokenClaims = jwtOutPort.parseAccessToken(accessToken)
}
