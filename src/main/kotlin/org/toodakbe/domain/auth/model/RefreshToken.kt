package org.toodakbe.domain.auth.model

import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.auth.vo.RefreshTokenId
import org.toodakbe.domain.member.vo.MemberId
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.HexFormat

/**
 * RefreshToken — Access Token 재발급 인증서.
 *
 * 도메인 불변식:
 * - DB엔 [tokenHash]만 저장. 평문 토큰은 발급 시점에 한 번만 클라이언트에 전달.
 * - 발급은 디바이스 단위(`memberId + deviceId`). 같은 디바이스 재로그인 시 기존 활성 토큰을 revoke한다.
 * - 회전(rotate) 시 새 토큰은 [rotatedFromId]로 이전 토큰을 참조 — 탈취 감지에 사용.
 * - 한 번 [revokedAt]이 채워지면 다시 비울 수 없다 (단방향 상태 전이, 멱등).
 *
 * 검증 책임은 UseCase가 진다 — [isRevoked]/[isExpired] 같은 쿼리 메서드만 노출.
 */
class RefreshToken private constructor(
    val id: RefreshTokenId,
    val memberId: MemberId,
    val tokenHash: String,
    val deviceId: DeviceId,
    val deviceLabel: String?,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val rotatedFromId: RefreshTokenId?,
) {
    fun isRevoked(): Boolean = revokedAt != null

    fun isExpired(now: Instant): Boolean = !now.isBefore(expiresAt)

    /**
     * 이 토큰을 폐기 상태로 전환한다. 이미 폐기된 경우 멱등.
     */
    fun revoke(now: Instant): RefreshToken {
        if (revokedAt != null) return this
        return RefreshToken(
            id = id,
            memberId = memberId,
            tokenHash = tokenHash,
            deviceId = deviceId,
            deviceLabel = deviceLabel,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            revokedAt = now,
            rotatedFromId = rotatedFromId,
        )
    }

    /**
     * 회전: 이 토큰을 기반으로 새 RefreshToken을 발급한다.
     *
     * 이 메서드는 새 토큰만 생성한다. 현재 토큰의 [revoke] 호출과 영속화는 UseCase가 한 트랜잭션으로 묶는다.
     */
    fun rotate(
        now: Instant,
        ttl: Duration,
    ): IssuedRefreshToken {
        val (plain, hash) = generatePlainAndHash()
        val rotated =
            RefreshToken(
                id = RefreshTokenId.generate(),
                memberId = memberId,
                tokenHash = hash,
                deviceId = deviceId,
                deviceLabel = deviceLabel,
                issuedAt = now,
                expiresAt = now.plus(ttl),
                revokedAt = null,
                rotatedFromId = id,
            )
        return IssuedRefreshToken.of(rotated, plain)
    }

    companion object {
        /**
         * 신규 RefreshToken 발급. 평문은 [IssuedRefreshToken.plainValue]로만 노출되고
         * DB엔 해시만 저장된다.
         */
        fun issue(
            memberId: MemberId,
            deviceId: DeviceId,
            deviceLabel: String?,
            ttl: Duration,
            now: Instant,
        ): IssuedRefreshToken {
            val (plain, hash) = generatePlainAndHash()
            val token =
                RefreshToken(
                    id = RefreshTokenId.generate(),
                    memberId = memberId,
                    tokenHash = hash,
                    deviceId = deviceId,
                    deviceLabel = deviceLabel,
                    issuedAt = now,
                    expiresAt = now.plus(ttl),
                    revokedAt = null,
                    rotatedFromId = null,
                )
            return IssuedRefreshToken.of(token, plain)
        }

        /**
         * 영속화 어댑터에서 도메인 모델을 복원할 때 사용.
         */
        fun restore(
            id: RefreshTokenId,
            memberId: MemberId,
            tokenHash: String,
            deviceId: DeviceId,
            deviceLabel: String?,
            issuedAt: Instant,
            expiresAt: Instant,
            revokedAt: Instant?,
            rotatedFromId: RefreshTokenId?,
        ): RefreshToken =
            RefreshToken(
                id = id,
                memberId = memberId,
                tokenHash = tokenHash,
                deviceId = deviceId,
                deviceLabel = deviceLabel,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
                revokedAt = revokedAt,
                rotatedFromId = rotatedFromId,
            )

        /**
         * 평문 토큰을 DB 조회용 해시로 변환한다.
         *
         * `RefreshTokenOutPort.findByHash(hashOfPlain(plain))` 형태로 UseCase에서 호출.
         * 해시 알고리즘이 도메인 책임이므로 어댑터가 직접 해시하지 않는다.
         */
        fun hashOfPlain(plain: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(plain.toByteArray(Charsets.UTF_8))
            return HexFormat.of().formatHex(bytes)
        }

        private fun generatePlainAndHash(): Pair<String, String> {
            val randomBytes = ByteArray(TOKEN_BYTE_LENGTH)
            SecureRandom().nextBytes(randomBytes)
            val plain = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
            return plain to hashOfPlain(plain)
        }

        private const val TOKEN_BYTE_LENGTH = 32
    }
}
