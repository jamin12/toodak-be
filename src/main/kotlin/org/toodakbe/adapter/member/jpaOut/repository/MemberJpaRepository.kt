package org.toodakbe.adapter.member.jpaOut.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.toodakbe.adapter.member.jpaOut.entity.MemberEntity
import java.util.UUID

interface MemberJpaRepository : JpaRepository<MemberEntity, UUID> {
    /**
     * 이메일로 회원을 조회한다. 이메일은 UNIQUE 가 아니므로 동일 이메일이 여러 명일 수 있다.
     * 가입 시각이 가장 빠른 회원을 우선 반환 (자동 연결 정책상 가장 오래된 ACTIVE 후보를 안정적으로 선택).
     */
    fun findFirstByEmailOrderByCreatedAtAsc(email: String): MemberEntity?
}
