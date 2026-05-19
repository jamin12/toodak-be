package org.toodakbe.adapter.auth.restIn.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.toodakbe.adapter.auth.restIn.dto.LoginWithGoogleRequest
import org.toodakbe.adapter.auth.restIn.dto.TokenPairResponse
import org.toodakbe.adapter.auth.restIn.mapper.AuthRestMapper
import org.toodakbe.adapter.common.restIn.dto.CommonResponse
import org.toodakbe.application.auth.port.inbound.LoginWithGoogleInPort

/**
 * 인증 v1 컨트롤러.
 *
 * - `POST /api/v1/auth/google`: Google ID Token 으로 로그인 + 가입 (1차 스코프)
 * - `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout` 는 Phase 4 에서 추가
 */
@Tag(name = "Auth", description = "인증/인가 (Google ID Token Flow + JWT)")
@RestController
@RequestMapping("/api/v1/auth")
class AuthV1Controller(
    private val loginWithGoogleInPort: LoginWithGoogleInPort,
    private val authRestMapper: AuthRestMapper,
) {
    @Operation(
        summary = "Google ID Token 로그인 / 자동 가입",
        description = """
            모바일 클라이언트가 Google SDK 로 받은 ID Token 을 검증해 로그인을 처리한다.
            - SocialIdentity 존재: 재로그인 (이메일 변경 시 동기화, WITHDRAWN 거부)
            - SocialIdentity 없음 + 같은 이메일 ACTIVE 회원 존재 + Google 이메일 검증됨: 자동 연결
            - 그 외: 새 Member 생성 후 SocialIdentity 연결
            응답의 `accessToken` 을 보호된 API 호출 시 Authorization: Bearer 헤더로 사용한다.
            (Refresh Token 은 Phase 4 에서 채워진다.)
        """,
    )
    @SecurityRequirements
    @PostMapping("/google")
    fun loginWithGoogle(
        @Valid @RequestBody request: LoginWithGoogleRequest,
    ): ResponseEntity<CommonResponse<TokenPairResponse?>> {
        val result = loginWithGoogleInPort.execute(authRestMapper.toCommand(request))
        return ResponseEntity.ok(CommonResponse.ok(authRestMapper.toResponse(result)))
    }
}
