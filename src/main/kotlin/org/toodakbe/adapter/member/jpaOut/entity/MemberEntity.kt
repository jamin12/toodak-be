package org.toodakbe.adapter.member.jpaOut.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.toodakbe.domain.member.enums.MemberStatus
import java.time.Instant
import java.util.UUID

/**
 * member 테이블 JPA Entity.
 *
 * 도메인 모델([org.toodakbe.domain.member.model.Member])과 분리해 영속성 관심사만 담는다.
 * `id`는 도메인이 UUID v7로 생성하므로 `@GeneratedValue`를 사용하지 않는다.
 */
@Entity
@Table(name = "member", comment = "회원")
class MemberEntity(
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "UUID", comment = "회원 ID (UUID v7)")
    val id: UUID,
    @Column(name = "email", nullable = false, length = 320, comment = "회원 이메일 (부가 정보, UNIQUE 아님)")
    var email: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, comment = "회원 상태 (ACTIVE | WITHDRAWN)")
    var status: MemberStatus,
    @Column(name = "created_at", nullable = false, comment = "가입 시각")
    val createdAt: Instant,
    @Column(name = "withdrawn_at", nullable = true, comment = "탈퇴 시각 (NULL 이면 ACTIVE)")
    var withdrawnAt: Instant?,
)
