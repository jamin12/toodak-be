package org.toodakbe.application.auth.port.inbound

import org.toodakbe.application.auth.dto.RefreshAccessTokenCommand
import org.toodakbe.application.auth.dto.TokenPairResult

/**
 * Refresh Token 회전 (Access Token 재발급) InPort.
 *
 * 정상 흐름: 기존 Refresh Token 폐기 + 새 Refresh/Access Token 발급.
 * 비정상 흐름:
 * - 토큰 미존재: [org.toodakbe.application.auth.exception.InvalidRefreshTokenException]
 * - 이미 폐기됨(재사용): [org.toodakbe.application.auth.exception.RefreshTokenReuseDetectedException]
 *   (해당 회원의 모든 활성 토큰을 일괄 회수한 뒤 예외)
 * - 만료됨: [org.toodakbe.application.auth.exception.RefreshTokenExpiredException]
 * - 디바이스 불일치: [org.toodakbe.application.auth.exception.RefreshTokenDeviceMismatchException]
 */
interface RefreshAccessTokenInPort {
    fun execute(command: RefreshAccessTokenCommand): TokenPairResult
}
