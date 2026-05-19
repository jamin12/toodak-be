package org.toodakbe.adapter.auth.jpaOut.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.toodakbe.adapter.auth.jpaOut.entity.RefreshTokenEntity
import java.util.UUID

/**
 * Spring Data 메서드 파생으로 표현 가능한 단순 쿼리만 둔다.
 * bulk UPDATE 같은 동적/일괄 작업은 [RefreshTokenJpaRepositoryCustom] (QueryDSL) 로 위임한다.
 */
interface RefreshTokenJpaRepository :
    JpaRepository<RefreshTokenEntity, UUID>,
    RefreshTokenJpaRepositoryCustom {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?

    /**
     * (memberId, deviceId) 의 활성(미회수) Refresh Token 을 조회한다.
     *
     * 같은 디바이스에서 재로그인이 들어왔을 때 기존 활성 토큰을 회수하기 위해 사용.
     */
    fun findFirstByMemberIdAndDeviceIdAndRevokedAtIsNull(
        memberId: UUID,
        deviceId: String,
    ): RefreshTokenEntity?
}
