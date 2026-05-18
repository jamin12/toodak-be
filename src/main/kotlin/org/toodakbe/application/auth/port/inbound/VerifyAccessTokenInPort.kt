package org.toodakbe.application.auth.port.inbound

import org.toodakbe.application.auth.dto.AccessTokenClaims

/**
 * Access Token을 검증하고 클레임을 반환하는 InPort.
 *
 * driving adapter(JwtAuthenticationFilter)가 호출한다.
 * 토큰 형식 검증은 [org.toodakbe.application.auth.port.outbound.JwtOutPort]에 위임하고,
 * 이 UseCase는 향후 추가될 비즈니스 규칙(예: 탈퇴 회원 거부, 블랙리스트 검증 등)을 모은다.
 *
 * 검증 실패 시 [org.toodakbe.application.auth.exception.InvalidAccessTokenException]을 던진다.
 */
interface VerifyAccessTokenInPort {
    fun verify(accessToken: String): AccessTokenClaims
}
