package org.toodakbe.adapter.member.jpaOut.mapper

import org.toodakbe.adapter.member.jpaOut.entity.MemberEntity
import org.toodakbe.domain.member.model.Member
import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.MemberId

/**
 * Member ↔ MemberEntity 매퍼 (확장함수 패턴).
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
