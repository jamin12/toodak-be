package org.toodakbe.adapter.auth.jwtOut

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * JWT 설정 바인딩.
 *
 * application.yaml의 `jwt.*` 키를 매핑한다.
 * - [secret] 운영 환경에선 환경변수 `JWT_SECRET`로 외부 주입 (32바이트 이상)
 * - [accessTtl] Access Token 유효 기간 (예: PT15M)
 * - [refreshTtl] Refresh Token 유효 기간 (예: P30D)
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTtl: Duration,
    val refreshTtl: Duration,
)
