package org.toodakbe.adapter.member.jpaOut.mapper

import org.toodakbe.adapter.member.jpaOut.entity.SocialIdentityEntity
import org.toodakbe.domain.member.model.SocialIdentity
import org.toodakbe.domain.member.vo.MemberId
import org.toodakbe.domain.member.vo.ProviderUserId
import org.toodakbe.domain.member.vo.SocialIdentityId

fun SocialIdentity.toEntity(): SocialIdentityEntity =
    SocialIdentityEntity(
        id = this.id.value,
        memberId = this.memberId.value,
        provider = this.provider,
        providerUserId = this.providerUserId.value,
        emailVerifiedAt = this.emailVerifiedAt,
        linkedAt = this.linkedAt,
    )

fun SocialIdentityEntity.toDomain(): SocialIdentity =
    SocialIdentity.restore(
        id = SocialIdentityId.from(this.id),
        memberId = MemberId.from(this.memberId),
        provider = this.provider,
        providerUserId = ProviderUserId(this.providerUserId),
        emailVerifiedAt = this.emailVerifiedAt,
        linkedAt = this.linkedAt,
    )
