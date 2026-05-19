package org.toodakbe.adapter.member.jpaOut.mapper

import org.toodakbe.adapter.member.jpaOut.entity.MemberEntity
import org.toodakbe.domain.member.model.Member
import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.MemberId

/**
 * Member ↔ MemberEntity 매퍼 (확장함수 패턴).
 *
 * Domain → Entity: 신규 저장 시.
 * Entity → Domain: 영속화 어댑터에서 복원 시 `Member.restore` 사용.
 * Entity.update: 기존 행 갱신 (영속 컨텍스트에서 더티 체킹).
 */
fun Member.toEntity(): MemberEntity =
    MemberEntity(
        id = this.id.value,
        email = this.email.value,
        status = this.status,
        createdAt = this.createdAt,
        withdrawnAt = this.withdrawnAt,
    )

fun MemberEntity.toDomain(): Member =
    Member.restore(
        id = MemberId.from(this.id),
        email = Email(this.email),
        status = this.status,
        createdAt = this.createdAt,
        withdrawnAt = this.withdrawnAt,
    )

fun MemberEntity.update(domain: Member): MemberEntity {
    this.email = domain.email.value
    this.status = domain.status
    this.withdrawnAt = domain.withdrawnAt
    return this
}
