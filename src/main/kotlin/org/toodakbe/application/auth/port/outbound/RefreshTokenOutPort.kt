package org.toodakbe.application.auth.port.outbound

import org.toodakbe.domain.auth.model.RefreshToken
import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

/**
 * refresh_token 테이블 영속성 OutPort.
 *
 * RefreshToken 의 발급/회전/회수에 필요한 모든 행위를 한 곳에 모은다.
 * 평문 토큰은 도메인 모델/Out Port 어디에서도 보관하지 않으며,
 * 조회는 `RefreshToken.hashOfPlain(plain)` 으로 변환한 해시값으로만 수행한다.
 */
interface RefreshTokenOutPort {
    fun save(token: RefreshToken): RefreshToken

    fun findByHash(tokenHash: String): RefreshToken?

    /**
     * (memberId, deviceId) 의 활성(미회수) 토큰을 조회한다.
     *
     * 같은 디바이스에서 로그인 요청이 들어왔을 때 기존 활성 토큰을 회수하기 위해 사용.
     */
    fun findActiveBy(
        memberId: MemberId,
        deviceId: DeviceId,
    ): RefreshToken?

    /**
     * 한 회원의 모든 활성 Refresh Token 을 일괄 회수한다 — 탈취 감지 시 호출.
     *
     * @return 실제로 회수된 토큰 수
     */
    fun revokeAllByMember(
        memberId: MemberId,
        revokedAt: Instant,
    ): Int
}
