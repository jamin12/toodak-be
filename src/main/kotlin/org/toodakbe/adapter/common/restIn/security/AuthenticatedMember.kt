package org.toodakbe.adapter.common.restIn.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.toodakbe.domain.member.vo.MemberId

/**
 * 인증된 회원을 표현하는 Spring Security `Authentication` 구현.
 *
 * `JwtAuthenticationFilter`가 Access Token 검증 후 SecurityContext에 채워둔다.
 * 보호된 컨트롤러는 `@AuthenticationPrincipal AuthenticatedMember`로 주입받아 [memberId]를 사용한다.
 *
 * 권한 모델(role/scope)이 도입되기 전이라 GrantedAuthority는 비어 있다.
 */
class AuthenticatedMember(
    val memberId: MemberId,
) : AbstractAuthenticationToken(emptyList()) {
    init {
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): Any = memberId

    override fun setAuthenticated(authenticated: Boolean) {
        if (authenticated) {
            throw IllegalArgumentException("AuthenticatedMember는 생성 시점에만 인증 상태가 결정됩니다.")
        }
        super.setAuthenticated(false)
    }
}
