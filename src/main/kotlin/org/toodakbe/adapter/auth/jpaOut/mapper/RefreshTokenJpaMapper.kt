package org.toodakbe.adapter.auth.jpaOut.mapper

import org.toodakbe.adapter.auth.jpaOut.entity.RefreshTokenEntity
import org.toodakbe.domain.auth.model.RefreshToken
import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.auth.vo.RefreshTokenId
import org.toodakbe.domain.member.vo.MemberId

/**
 * RefreshToken ↔ RefreshTokenEntity 매퍼 (확장함수 패턴).
 */
fun RefreshToken.toEntity(): RefreshTokenEntity =
    RefreshTokenEntity(
        id = this.id.value,
        memberId = this.memberId.value,
        tokenHash = this.tokenHash,
        deviceId = this.deviceId.value,
        deviceLabel = this.deviceLabel,
        issuedAt = this.issuedAt,
        expiresAt = this.expiresAt,
        revokedAt = this.revokedAt,
        rotatedFromId = this.rotatedFromId?.value,
    )

fun RefreshTokenEntity.toDomain(): RefreshToken =
    RefreshToken.restore(
        id = RefreshTokenId.from(this.id),
        memberId = MemberId.from(this.memberId),
        tokenHash = this.tokenHash,
        deviceId = DeviceId(this.deviceId),
        deviceLabel = this.deviceLabel,
        issuedAt = this.issuedAt,
        expiresAt = this.expiresAt,
        revokedAt = this.revokedAt,
        rotatedFromId = this.rotatedFromId?.let { RefreshTokenId.from(it) },
    )
