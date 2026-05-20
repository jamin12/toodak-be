package org.toodakbe.adapter.auth.jpaOut

import org.springframework.stereotype.Component
import org.toodakbe.adapter.auth.jpaOut.mapper.toDomain
import org.toodakbe.adapter.auth.jpaOut.mapper.toEntity
import org.toodakbe.adapter.auth.jpaOut.repository.RefreshTokenJpaRepository
import org.toodakbe.application.auth.port.outbound.RefreshTokenOutPort
import org.toodakbe.domain.auth.model.RefreshToken
import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

/**
 * [RefreshTokenOutPort] 의 JPA 구현체.
 */
@Component
class RefreshTokenJpaAdapter(
    private val repository: RefreshTokenJpaRepository,
) : RefreshTokenOutPort {
    override fun save(token: RefreshToken): RefreshToken = repository.save(token.toEntity()).toDomain()

    override fun findByHash(tokenHash: String): RefreshToken? = repository.findByTokenHash(tokenHash)?.toDomain()

    override fun findActiveBy(
        memberId: MemberId,
        deviceId: DeviceId,
    ): RefreshToken? = repository.findFirstByMemberIdAndDeviceIdAndRevokedAtIsNull(memberId.value, deviceId.value)?.toDomain()

    override fun revokeAllByMember(
        memberId: MemberId,
        revokedAt: Instant,
    ): Int = repository.revokeAllActiveByMemberId(memberId.value, revokedAt).toInt()
}
