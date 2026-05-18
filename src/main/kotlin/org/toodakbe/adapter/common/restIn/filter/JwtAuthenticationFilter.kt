package org.toodakbe.adapter.common.restIn.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import org.toodakbe.adapter.common.restIn.security.AuthenticatedMember
import org.toodakbe.application.auth.exception.InvalidAccessTokenException
import org.toodakbe.application.auth.port.inbound.VerifyAccessTokenInPort

/**
 * 모든 HTTP 요청에 대해 Authorization 헤더의 Bearer 토큰을 검증하고
 * SecurityContext에 [AuthenticatedMember]를 채운다.
 *
 * 동작:
 * - Authorization 헤더가 없거나 Bearer 접두어가 아니면 통과 (다음 필터가 처리)
 * - 토큰 검증 실패 시 SecurityContext를 비우고 통과 — 이후 보호된 엔드포인트에서 [org.toodakbe.adapter.common.restIn.security.AuthEntryPoint]가 401 반환
 * - 검증 성공 시 [AuthenticatedMember]를 SecurityContext에 등록
 *
 * 정책: 토큰이 잘못되어도 즉시 401로 끊지 않는다. permitAll 경로(`/api/v1/auth/google` 등)에서는
 * 토큰이 없거나 잘못되어도 요청이 통과해야 하므로, 401 결정은 [org.toodakbe.adapter.common.restIn.security.AuthEntryPoint]에 위임한다.
 *
 * 헥사고날 원칙에 따라 driving adapter는 OutPort를 직접 호출하지 않고
 * [VerifyAccessTokenInPort]를 통해 application 레이어에 검증 책임을 위임한다.
 */
class JwtAuthenticationFilter(
    private val verifyAccessTokenInPort: VerifyAccessTokenInPort,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            val token = header.substring(BEARER_PREFIX.length).trim()
            try {
                val claims = verifyAccessTokenInPort.verify(token)
                SecurityContextHolder.getContext().authentication = AuthenticatedMember(claims.memberId)
            } catch (_: InvalidAccessTokenException) {
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
