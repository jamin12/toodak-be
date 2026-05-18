package org.toodakbe.adapter.common.restIn.security

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Spring Security 설정 프로퍼티.
 *
 * application.yaml의 `app.security.*` 키를 매핑한다. CCP의 `SecurityProps` 패턴을 단순화하여 적용.
 *
 * @property publicPaths 인증 없이 접근 가능한 경로 목록. Ant 패턴(와일드카드) 지원.
 *   환경별 추가가 필요하면 Spring profile별 yaml(application-{profile}.yaml)에서 override한다.
 */
@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(
    val publicPaths: List<String> = emptyList(),
)
