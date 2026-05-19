package org.toodakbe.adapter.auth.jpaOut.repository

import java.time.Instant
import java.util.UUID

interface RefreshTokenJpaRepositoryCustom {
    /**
     * 한 회원의 활성 Refresh Token 을 일괄 회수한다 — 탈취 감지 시 호출.
     *
     * Bulk UPDATE 이므로 영속성 컨텍스트의 1차 캐시를 우회한다. 호출 측은 같은 트랜잭션에서
     * 이전에 로드한 토큰 엔티티를 재사용하지 않도록 주의한다 (구현체에서 `flush/clear` 수행).
     *
     * @return 실제로 회수된 토큰 수
     */
    fun revokeAllActiveByMemberId(
        memberId: UUID,
        revokedAt: Instant,
    ): Long
}
