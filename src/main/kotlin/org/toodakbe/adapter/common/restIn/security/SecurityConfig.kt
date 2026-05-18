package org.toodakbe.adapter.common.restIn.security

import jakarta.servlet.DispatcherType
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.toodakbe.adapter.auth.jwtOut.JwtProperties
import org.toodakbe.adapter.common.restIn.filter.JwtAuthenticationFilter
import org.toodakbe.application.auth.port.inbound.VerifyAccessTokenInPort

/**
 * Spring Security 필터 체인 구성.
 *
 * 정책:
 * - CSRF 비활성화 (토큰 기반, stateless)
 * - 세션 STATELESS
 * - formLogin/httpBasic 비활성화
 * - 공개 경로는 [SecurityProperties.publicPaths]에서 외부 주입 (application.yaml `app.security.public-paths`)
 * - 에러 forward(DispatcherType.ERROR)는 항상 permitAll — Spring Boot가 /error로 내부 forward할 때
 *   원래 응답이 401로 가려지는 것을 막는다. yaml의 public-paths에도 /error 및 하위 경로를 두어 이중 안전망.
 * - 나머지는 인증 필요
 * - `UsernamePasswordAuthenticationFilter` 앞에 [JwtAuthenticationFilter] 등록
 */
@Configuration
@EnableConfigurationProperties(JwtProperties::class, SecurityProperties::class)
class SecurityConfig(
    private val securityProperties: SecurityProperties,
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        verifyAccessTokenInPort: VerifyAccessTokenInPort,
        authEntryPoint: AuthEntryPoint,
        accessDeniedHandler: AccessDeniedHandlerImpl,
    ): SecurityFilterChain {
        val publicPaths = securityProperties.publicPaths.toTypedArray()
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests { authz ->
                authz.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                if (publicPaths.isNotEmpty()) {
                    authz.requestMatchers(*publicPaths).permitAll()
                }
                authz.anyRequest().authenticated()
            }.exceptionHandling { eh ->
                eh.authenticationEntryPoint(authEntryPoint)
                eh.accessDeniedHandler(accessDeniedHandler)
            }.addFilterBefore(
                JwtAuthenticationFilter(verifyAccessTokenInPort),
                UsernamePasswordAuthenticationFilter::class.java,
            )
        return http.build()
    }
}
