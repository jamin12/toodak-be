package org.toodakbe.adapter.auth.jpaOut.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

/**
 * refresh_token 테이블 JPA Entity.
 *
 * - 평문 토큰은 저장하지 않고 [tokenHash] 만 보유.
 * - [rotatedFromId] 는 회전 직전 토큰을 가리키는 자기 참조 (외래키는 두지 않음 — 회수 시 cascade 단순화).
 * - 도메인 모델([org.toodakbe.domain.auth.model.RefreshToken]) 과 분리해 영속성 관심사만 담는다.
 */
@Entity
@Table(
    name = "refresh_token",
    comment = "Refresh Token (디바이스 단위 세션)",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_refresh_token_token_hash",
            columnNames = ["token_hash"],
        ),
    ],
    indexes = [
        Index(name = "ix_refresh_token_member_device", columnList = "member_id, device_id"),
        Index(name = "ix_refresh_token_member_id", columnList = "member_id"),
    ],
)
class RefreshTokenEntity(
    id: UUID,
    memberId: UUID,
    tokenHash: String,
    deviceId: String,
    deviceLabel: String?,
    issuedAt: Instant,
    expiresAt: Instant,
    revokedAt: Instant?,
    rotatedFromId: UUID?,
) {
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "UUID", comment = "Refresh Token ID (UUID v7)")
    var id: UUID = id
        protected set

    @Column(name = "member_id", nullable = false, columnDefinition = "UUID", comment = "회원 ID (FK -> member.id)")
    var memberId: UUID = memberId
        protected set

    @Column(
        name = "token_hash",
        nullable = false,
        length = 128,
        comment = "평문 토큰의 SHA-256 해시 (HEX). 평문은 저장하지 않는다.",
    )
    var tokenHash: String = tokenHash
        protected set

    @Column(name = "device_id", nullable = false, length = 255, comment = "클라이언트 디바이스 식별자 (UUID 등)")
    var deviceId: String = deviceId
        protected set

    @Column(
        name = "device_label",
        nullable = true,
        length = 255,
        comment = "사용자/관리자가 인식할 수 있는 디바이스 라벨",
    )
    var deviceLabel: String? = deviceLabel
        protected set

    @Column(name = "issued_at", nullable = false, comment = "발급 시각")
    var issuedAt: Instant = issuedAt
        protected set

    @Column(name = "expires_at", nullable = false, comment = "만료 시각 (now + refresh-ttl)")
    var expiresAt: Instant = expiresAt
        protected set

    @Column(
        name = "revoked_at",
        nullable = true,
        comment = "폐기 시각 (NULL 이면 활성). 한 번 채워지면 비울 수 없다.",
    )
    var revokedAt: Instant? = revokedAt
        protected set

    @Column(
        name = "rotated_from_id",
        nullable = true,
        columnDefinition = "UUID",
        comment = "회전 직전 토큰의 ID (자기 참조). 탈취 감지에 사용.",
    )
    var rotatedFromId: UUID? = rotatedFromId
        protected set
}
