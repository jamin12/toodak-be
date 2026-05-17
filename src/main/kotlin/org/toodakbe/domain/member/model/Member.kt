package org.toodakbe.domain.member.model

import org.toodakbe.domain.member.enums.MemberStatus
import org.toodakbe.domain.member.exception.MemberNotActiveException
import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

/**
 * 회원 애그리거트 루트.
 *
 * 도메인 불변식:
 * - `id`는 영구 불변 (UUID v7)
 * - `email`은 부가 정보 — unique 제약 없음, Google 응답으로 동기화 가능
 * - `WITHDRAWN` 상태에선 어떤 변경 작업도 불가
 *
 * 상태 머신: `ACTIVE → WITHDRAWN` (단방향, soft delete)
 *
 * 모든 필드는 `val`이며 상태 변경 메서드는 새 인스턴스를 반환한다.
 */
class Member private constructor(
    val id: MemberId,
    val email: Email,
    val status: MemberStatus,
    val createdAt: Instant,
    val withdrawnAt: Instant?,
) {
    /**
     * Google 응답의 이메일을 동기화한다.
     *
     * @throws MemberNotActiveException ACTIVE 상태가 아닌 경우
     */
    fun changeEmail(newEmail: Email): Member {
        requireActive()
        if (email == newEmail) return this
        return Member(
            id = id,
            email = newEmail,
            status = status,
            createdAt = createdAt,
            withdrawnAt = withdrawnAt,
        )
    }

    /**
     * 회원을 탈퇴 상태로 전환한다. 이미 WITHDRAWN이면 멱등.
     */
    fun withdraw(now: Instant): Member {
        if (status == MemberStatus.WITHDRAWN) return this
        return Member(
            id = id,
            email = email,
            status = MemberStatus.WITHDRAWN,
            createdAt = createdAt,
            withdrawnAt = now,
        )
    }

    private fun requireActive() {
        if (status != MemberStatus.ACTIVE) {
            throw MemberNotActiveException(id, status)
        }
    }

    companion object {
        /**
         * 신규 회원 가입. `id`는 UUID v7로 자동 생성된다.
         */
        fun register(
            email: Email,
            now: Instant,
        ): Member =
            Member(
                id = MemberId.generate(),
                email = email,
                status = MemberStatus.ACTIVE,
                createdAt = now,
                withdrawnAt = null,
            )

        /**
         * 영속화 어댑터에서 도메인 모델을 복원할 때 사용.
         */
        fun restore(
            id: MemberId,
            email: Email,
            status: MemberStatus,
            createdAt: Instant,
            withdrawnAt: Instant?,
        ): Member =
            Member(
                id = id,
                email = email,
                status = status,
                createdAt = createdAt,
                withdrawnAt = withdrawnAt,
            )
    }
}
