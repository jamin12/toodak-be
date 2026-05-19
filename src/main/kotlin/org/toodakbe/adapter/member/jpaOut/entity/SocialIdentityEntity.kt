package org.toodakbe.adapter.member.jpaOut.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.toodakbe.domain.member.vo.Provider
import java.time.Instant
import java.util.UUID

/**
 * social_identity 테이블 JPA Entity.
 *
 * `(provider, providerUserId)` 조합은 시스템 전체에서 UNIQUE.
 * JPA 매핑에선 컬럼만 들고, 도메인 모델([org.toodakbe.domain.member.model.SocialIdentity])과 분리된다.
 */
@Entity
@Table(
    name = "social_identity",
    comment = "소셜 제공자 연결",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_social_identity_provider_user",
            columnNames = ["provider", "provider_user_id"],
        ),
    ],
    indexes = [
        Index(name = "ix_social_identity_member_id", columnList = "member_id"),
    ],
)
class SocialIdentityEntity(
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "UUID", comment = "소셜 연결 ID (UUID v7)")
    val id: UUID,
    @Column(name = "member_id", nullable = false, columnDefinition = "UUID", comment = "회원 ID (FK -> member.id)")
    val memberId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20, comment = "소셜 제공자 (GOOGLE | KAKAO | APPLE)")
    val provider: Provider,
    @Column(
        name = "provider_user_id",
        nullable = false,
        length = 255,
        comment = "제공자가 발급한 영구 사용자 식별자",
    )
    val providerUserId: String,
    @Column(
        name = "email_verified_at",
        nullable = true,
        comment = "제공자가 이메일을 검증한 시각 (자동 연결 정책 판단용)",
    )
    val emailVerifiedAt: Instant?,
    @Column(name = "linked_at", nullable = false, comment = "연결 시각")
    val linkedAt: Instant,
)
