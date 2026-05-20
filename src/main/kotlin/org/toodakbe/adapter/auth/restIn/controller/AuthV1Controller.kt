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
import org.toodakbe.adapter.auth.restIn.dto.LogoutRequest
import org.toodakbe.adapter.auth.restIn.dto.RefreshAccessTokenRequest
import org.toodakbe.adapter.auth.restIn.dto.TokenPairResponse
import org.toodakbe.adapter.auth.restIn.mapper.AuthRestMapper
import org.toodakbe.adapter.common.restIn.dto.CommonResponse
import org.toodakbe.application.auth.port.inbound.LoginWithGoogleInPort
import org.toodakbe.application.auth.port.inbound.LogoutInPort
import org.toodakbe.application.auth.port.inbound.RefreshAccessTokenInPort

@Tag(name = "Auth", description = "인증/인가 (Google ID Token Flow + JWT)")
@RestController
@RequestMapping("/api/v1/auth")
class AuthV1Controller(
    private val loginWithGoogleInPort: LoginWithGoogleInPort,
    private val refreshAccessTokenInPort: RefreshAccessTokenInPort,
    private val logoutInPort: LogoutInPort,
    private val authRestMapper: AuthRestMapper,
) {
    @Operation(
        summary = "Google ID Token 로그인 / 자동 가입",
        description = """
            모바일 클라이언트가 Google SDK 로 받은 ID Token 을 검증해 로그인을 처리한다.
            - SocialIdentity 존재: 재로그인 (이메일 변경 시 동기화, WITHDRAWN 거부)
            - SocialIdentity 없음 + 같은 이메일 ACTIVE 회원 존재 + Google 이메일 검증됨: 자동 연결
            - 그 외: 새 Member 생성 후 SocialIdentity 연결
            응답의 `accessToken` 을 보호된 API 호출 시 `Authorization: Bearer ...` 헤더로 사용한다.
            `refreshToken` 은 안전한 저장소(Keychain 등)에 보관한다.
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

    @Operation(
        summary = "Refresh Token 회전",
        description = """
            Refresh Token 으로 새 Access/Refresh Token 을 발급한다 (회전).
            정책:
            - 토큰 미존재: 401 INVALID_REFRESH_TOKEN
            - 이미 폐기된 토큰 재사용 감지: 해당 회원의 모든 활성 토큰 일괄 회수 후 401 TOKEN_REUSE_DETECTED
            - 만료: 401 REFRESH_EXPIRED
            - 디바이스 불일치: 401 DEVICE_MISMATCH
        """,
    )
    @SecurityRequirements
    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshAccessTokenRequest,
    ): ResponseEntity<CommonResponse<TokenPairResponse?>> {
        val result = refreshAccessTokenInPort.execute(authRestMapper.toCommand(request))
        return ResponseEntity.ok(CommonResponse.ok(authRestMapper.toResponse(result)))
    }

    @Operation(
        summary = "로그아웃",
        description = """
            디바이스 단위 Refresh Token 을 폐기한다.
            멱등 — 존재하지 않거나 이미 폐기된 토큰이어도 200 성공 응답.
            Access Token 은 stateless 라 즉시 무효화가 불가능하므로 짧은 TTL 로 만료를 기다린다.
        """,
    )
    @SecurityRequirements
    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ): ResponseEntity<CommonResponse<Unit?>> {
        logoutInPort.execute(authRestMapper.toCommand(request))
        return ResponseEntity.ok(CommonResponse.ok())
    }
}
