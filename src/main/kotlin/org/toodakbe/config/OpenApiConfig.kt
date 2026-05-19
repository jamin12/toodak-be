package org.toodakbe.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Springdoc OpenAPI 설정.
 *
 * - `bearerAuth` security scheme 을 전역으로 등록해 Swagger UI 에서 "Authorize" 버튼으로 Access Token 을 주입할 수 있다.
 * - 공개 엔드포인트(`/api/v1/auth/google` 등)는 `@SecurityRequirements` 또는 별도 표기로 해제 가능하다.
 */
@Configuration
class OpenApiConfig {
    @Bean
    fun toodakOpenAPI(): OpenAPI {
        val bearer =
            SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .`in`(SecurityScheme.In.HEADER)
                .name("Authorization")

        return OpenAPI()
            .info(
                Info()
                    .title("토닥 백엔드 API")
                    .description("Google ID Token 기반 모바일 인증 및 회원 도메인 API")
                    .version("v1"),
            ).components(Components().addSecuritySchemes(BEARER_KEY, bearer))
            .addSecurityItem(SecurityRequirement().addList(BEARER_KEY))
    }

    companion object {
        const val BEARER_KEY = "bearerAuth"
    }
}
