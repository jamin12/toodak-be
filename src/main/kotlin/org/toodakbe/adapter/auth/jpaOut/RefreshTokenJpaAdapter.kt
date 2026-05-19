package org.toodakbe.adapter.auth.jpaOut

import org.springframework.stereotype.Component
import org.toodakbe.adapter.auth.jpaOut.mapper.toDomain
import org.toodakbe.adapter.auth.jpaOut.mapper.toEntity
import org.toodakbe.adapter.auth.jpaOut.mapper.update
import org.toodakbe.adapter.auth.jpaOut.repository.RefreshTokenJpaRepository
import org.toodakbe.application.auth.port.outbound.RefreshTokenOutPort
import org.toodakbe.domain.auth.model.RefreshToken
import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

/**
 * [RefreshTokenOutPort] 의 JPA 구현체.
 *
 * id 가 도메인이 미리 생성한 UUID v7 이므로 신규/갱신 구분은 `findById` 로 한다.
 * 존재하면 영속 컨텍스트에서 가져와 [update], 없으면 새 엔티티 `save`.
 */
@Component
class RefreshTokenJpaAdapter(
    private val repository: RefreshTokenJpaRepository,
) : RefreshTokenOutPort {
    override fun save(token: RefreshToken): RefreshToken {
        val saved =
            repository
                .findById(token.id.value)
                .map { existing -> repository.save(existing.update(token)) }
                .orElseGet { repository.save(token.toEntity()) }
        return saved.toDomain()
    }

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
