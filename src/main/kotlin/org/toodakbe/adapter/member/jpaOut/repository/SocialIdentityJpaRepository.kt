package org.toodakbe.adapter.member.jpaOut.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.toodakbe.adapter.member.jpaOut.entity.SocialIdentityEntity
import org.toodakbe.domain.member.vo.Provider
import java.util.UUID

interface SocialIdentityJpaRepository : JpaRepository<SocialIdentityEntity, UUID> {
    fun findByProviderAndProviderUserId(
        provider: Provider,
        providerUserId: String,
    ): SocialIdentityEntity?
}
