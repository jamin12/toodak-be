package org.toodakbe.adapter.member.jpaOut

import org.springframework.stereotype.Component
import org.toodakbe.adapter.member.jpaOut.mapper.toDomain
import org.toodakbe.adapter.member.jpaOut.mapper.toEntity
import org.toodakbe.adapter.member.jpaOut.repository.MemberJpaRepository
import org.toodakbe.application.member.port.outbound.MemberOutPort
import org.toodakbe.domain.member.model.Member
import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.MemberId

/**
 * [MemberOutPort]의 JPA 구현체.
 */
@Component
class MemberJpaAdapter(
    private val repository: MemberJpaRepository,
) : MemberOutPort {
    override fun save(member: Member): Member = repository.save(member.toEntity()).toDomain()

    override fun findById(memberId: MemberId): Member? = repository.findById(memberId.value).map { it.toDomain() }.orElse(null)

    override fun findByEmail(email: Email): Member? = repository.findFirstByEmailOrderByCreatedAtAsc(email.value)?.toDomain()
}
