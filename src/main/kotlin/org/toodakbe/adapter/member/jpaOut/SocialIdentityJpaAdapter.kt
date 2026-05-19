package org.toodakbe.adapter.member.jpaOut

import org.springframework.stereotype.Component
import org.toodakbe.adapter.member.jpaOut.mapper.toDomain
import org.toodakbe.adapter.member.jpaOut.mapper.toEntity
import org.toodakbe.adapter.member.jpaOut.repository.SocialIdentityJpaRepository
import org.toodakbe.application.member.port.outbound.SocialIdentityOutPort
import org.toodakbe.domain.member.model.SocialIdentity
import org.toodakbe.domain.member.vo.Provider
import org.toodakbe.domain.member.vo.ProviderUserId

/**
 * [SocialIdentityOutPort]의 JPA 구현체.
 */
@Component
class SocialIdentityJpaAdapter(
    private val repository: SocialIdentityJpaRepository,
) : SocialIdentityOutPort {
    override fun save(identity: SocialIdentity): SocialIdentity = repository.save(identity.toEntity()).toDomain()

    override fun findBy(
        provider: Provider,
        providerUserId: ProviderUserId,
    ): SocialIdentity? = repository.findByProviderAndProviderUserId(provider, providerUserId.value)?.toDomain()
}
