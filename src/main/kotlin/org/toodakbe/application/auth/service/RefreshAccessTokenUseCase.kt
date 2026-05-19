package org.toodakbe.application.auth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.toodakbe.application.auth.dto.RefreshAccessTokenCommand
import org.toodakbe.application.auth.dto.TokenPairResult
import org.toodakbe.application.auth.exception.InvalidRefreshTokenException
import org.toodakbe.application.auth.exception.RefreshTokenDeviceMismatchException
import org.toodakbe.application.auth.exception.RefreshTokenExpiredException
import org.toodakbe.application.auth.exception.RefreshTokenReuseDetectedException
import org.toodakbe.application.auth.port.inbound.RefreshAccessTokenInPort
import org.toodakbe.application.auth.port.outbound.JwtOutPort
import org.toodakbe.application.auth.port.outbound.RefreshTokenOutPort
import org.toodakbe.domain.auth.model.RefreshToken
import org.toodakbe.domain.auth.vo.DeviceId
import java.time.Clock
import java.time.Duration

/**
 * Refresh Token 회전 UseCase.
 *
 * 정책:
 * - 토큰 미존재 → [InvalidRefreshTokenException]
 * - 이미 폐기됨(revoked) → 탈취로 간주, 해당 회원의 모든 활성 토큰 일괄 회수 후 [RefreshTokenReuseDetectedException]
 * - 만료 → [RefreshTokenExpiredException]
 * - 디바이스 불일치 → [RefreshTokenDeviceMismatchException]
 * - 정상 → 기존 토큰 revoke + 새 RefreshToken/AccessToken 발급
 */
@Service
class RefreshAccessTokenUseCase(
    private val refreshTokenOutPort: RefreshTokenOutPort,
    private val jwtOutPort: JwtOutPort,
    private val clock: Clock,
    @param:Value($$"${jwt.access-ttl}") private val accessTtl: Duration,
    @param:Value($$"${jwt.refresh-ttl}") private val refreshTtl: Duration,
) : RefreshAccessTokenInPort {
    // 탈취 감지 분기에선 RefreshTokenReuseDetectedException 을 던지지만,
    // 그 직전 호출한 `revokeAllByMember` 의 일괄 회수 결과는 반드시 커밋되어야 한다.
    // 기본 rollback 정책이 RuntimeException 을 롤백하므로 명시적으로 제외한다.
    @Transactional(noRollbackFor = [RefreshTokenReuseDetectedException::class])
    override fun execute(command: RefreshAccessTokenCommand): TokenPairResult {
        val now = clock.instant()
        val tokenHash = RefreshToken.hashOfPlain(command.refreshToken)
        val current = refreshTokenOutPort.findByHash(tokenHash) ?: throw InvalidRefreshTokenException()

        if (current.isRevoked()) {
            refreshTokenOutPort.revokeAllByMember(current.memberId, now)
            throw RefreshTokenReuseDetectedException()
        }
        if (current.isExpired(now)) {
            throw RefreshTokenExpiredException()
        }
        if (current.deviceId != DeviceId(command.deviceId)) {
            throw RefreshTokenDeviceMismatchException()
        }

        refreshTokenOutPort.save(current.revoke(now))
        val rotated = current.rotate(now, refreshTtl)
        refreshTokenOutPort.save(rotated.entity)

        val accessToken = jwtOutPort.issueAccessToken(current.memberId, accessTtl)
        return TokenPairResult(
            accessToken = accessToken.value,
            refreshToken = rotated.plainValue,
            expiresIn = accessTtl.seconds,
        )
    }
}
