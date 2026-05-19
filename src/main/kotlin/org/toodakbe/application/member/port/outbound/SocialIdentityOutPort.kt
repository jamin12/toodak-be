package org.toodakbe.application.member.port.outbound

import org.toodakbe.domain.member.model.SocialIdentity
import org.toodakbe.domain.member.vo.Provider
import org.toodakbe.domain.member.vo.ProviderUserId

/**
 * social_identity 테이블 영속성 OutPort.
 */
interface SocialIdentityOutPort {
    fun save(identity: SocialIdentity): SocialIdentity

    /**
     * (provider, providerUserId) 조합으로 기존 연결을 조회한다.
     * 이 조합은 시스템 전체에서 UNIQUE — 결과는 0개 또는 1개.
     */
    fun findBy(
        provider: Provider,
        providerUserId: ProviderUserId,
    ): SocialIdentity?
}
