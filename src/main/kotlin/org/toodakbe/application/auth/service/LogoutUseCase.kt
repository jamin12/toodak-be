package org.toodakbe.application.auth.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.toodakbe.application.auth.dto.LogoutCommand
import org.toodakbe.application.auth.port.inbound.LogoutInPort
import org.toodakbe.application.auth.port.outbound.RefreshTokenOutPort
import org.toodakbe.domain.auth.model.RefreshToken
import java.time.Clock

/**
 * 로그아웃 UseCase — 디바이스 단위 Refresh Token 폐기.
 *
 * 멱등 — 토큰이 존재하지 않거나 이미 폐기된 상태여도 예외 없이 정상 종료.
 * Access Token 은 stateless 라 즉시 무효화가 불가능하므로 짧은 TTL 로 만료를 기다린다.
 */
@Service
class LogoutUseCase(
    private val refreshTokenOutPort: RefreshTokenOutPort,
    private val clock: Clock,
) : LogoutInPort {
    @Transactional
    override fun execute(command: LogoutCommand) {
        val tokenHash = RefreshToken.hashOfPlain(command.refreshToken)
        val token = refreshTokenOutPort.findByHash(tokenHash) ?: return
        if (token.isRevoked()) return
        refreshTokenOutPort.save(token.revoke(clock.instant()))
    }
}
