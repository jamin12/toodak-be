package org.toodakbe.adapter.auth.jpaOut.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.toodakbe.adapter.auth.jpaOut.entity.QRefreshTokenEntity
import java.time.Instant
import java.util.UUID

class RefreshTokenJpaRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val entityManager: EntityManager,
) : RefreshTokenJpaRepositoryCustom {
    override fun revokeAllActiveByMemberId(
        memberId: UUID,
        revokedAt: Instant,
    ): Long {
        val rt = QRefreshTokenEntity.refreshTokenEntity
        val updated =
            queryFactory
                .update(rt)
                .set(rt.revokedAt, revokedAt)
                .where(
                    rt.memberId.eq(memberId),
                    rt.revokedAt.isNull,
                ).execute()
        entityManager.flush()
        entityManager.clear()
        return updated
    }
}
