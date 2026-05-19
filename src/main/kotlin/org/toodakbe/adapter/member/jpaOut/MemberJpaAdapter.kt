package org.toodakbe.adapter.member.jpaOut

import org.springframework.stereotype.Component
import org.toodakbe.adapter.member.jpaOut.mapper.toDomain
import org.toodakbe.adapter.member.jpaOut.mapper.toEntity
import org.toodakbe.adapter.member.jpaOut.mapper.update
import org.toodakbe.adapter.member.jpaOut.repository.MemberJpaRepository
import org.toodakbe.application.member.port.outbound.MemberOutPort
import org.toodakbe.domain.member.model.Member
import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.MemberId

/**
 * [MemberOutPort]의 JPA 구현체.
 *
 * id 가 도메인이 미리 생성한 UUID v7 이므로 신규/갱신 구분은 `findById`로 한다.
 * 존재하면 영속 컨텍스트에서 가져와 `update()`, 없으면 새 엔티티 `save()`.
 */
@Component
class MemberJpaAdapter(
    private val repository: MemberJpaRepository,
) : MemberOutPort {
    override fun save(member: Member): Member {
        val saved =
            repository
                .findById(member.id.value)
                .map { existing ->
                    repository.save(existing.update(member))
                }.orElseGet {
                    repository.save(member.toEntity())
                }
        return saved.toDomain()
    }

    override fun findById(memberId: MemberId): Member? = repository.findById(memberId.value).map { it.toDomain() }.orElse(null)

    override fun findByEmail(email: Email): Member? = repository.findFirstByEmailOrderByCreatedAtAsc(email.value)?.toDomain()
}
