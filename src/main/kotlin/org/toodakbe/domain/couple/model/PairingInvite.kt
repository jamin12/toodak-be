package org.toodakbe.domain.couple.model

import org.toodakbe.domain.couple.enums.InviteStatus
import org.toodakbe.domain.couple.vo.InviteCode
import org.toodakbe.domain.couple.vo.InviteId
import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

/**
 * 커플 페어링 초대(코드 = 공유 링크 토큰)와 그 진행 상태.
 *
 * 도메인 불변식:
 * - 1회용 — [accept]로 한 번 ACCEPTED가 되면 다시 사용할 수 없다.
 * - 활성(PENDING) 초대는 초대자당 하나만 둔다 — 재발급 시 기존 초대를 [revoke]하고 새로 발급(앱 레이어).
 * - 합류자/초대자가 이미 다른 커플에 속했는지는 교차 애그리거트라 앱 레이어에서 선검증한다.
 */
class PairingInvite private constructor(
    val id: InviteId,
    val code: InviteCode,
    val inviterId: MemberId,
    val status: InviteStatus,
    val createdAt: Instant,
    val expiresAt: Instant,
    val acceptedBy: MemberId?,
    val acceptedAt: Instant?,
) {
    fun usable(now: Instant): Boolean = status == InviteStatus.PENDING && now.isBefore(expiresAt)

    /**
     * 합류자가 초대를 수락해 연결을 확정한다. 성공 시 앱이 이어서 [Couple.create]를 호출한다.
     */
    fun accept(
        joinerId: MemberId,
        now: Instant,
    ): PairingInvite {
        require(usable(now)) { "사용할 수 없는 초대입니다" }
        require(joinerId != inviterId) { "자신의 초대 코드로는 연결할 수 없습니다" }
        return PairingInvite(
            id = id,
            code = code,
            inviterId = inviterId,
            status = InviteStatus.ACCEPTED,
            createdAt = createdAt,
            expiresAt = expiresAt,
            acceptedBy = joinerId,
            acceptedAt = now,
        )
    }

    /**
     * 초대를 무효화한다(재발급·취소). PENDING이 아니면 멱등.
     */
    fun revoke(): PairingInvite {
        if (status != InviteStatus.PENDING) return this
        return PairingInvite(
            id = id,
            code = code,
            inviterId = inviterId,
            status = InviteStatus.REVOKED,
            createdAt = createdAt,
            expiresAt = expiresAt,
            acceptedBy = acceptedBy,
            acceptedAt = acceptedAt,
        )
    }

    companion object {
        fun issue(
            code: InviteCode,
            inviterId: MemberId,
            expiresAt: Instant,
            now: Instant,
        ): PairingInvite =
            PairingInvite(
                id = InviteId.generate(),
                code = code,
                inviterId = inviterId,
                status = InviteStatus.PENDING,
                createdAt = now,
                expiresAt = expiresAt,
                acceptedBy = null,
                acceptedAt = null,
            )

        /**
         * 영속화 어댑터에서 도메인 모델을 복원할 때 사용.
         */
        fun restore(
            id: InviteId,
            code: InviteCode,
            inviterId: MemberId,
            status: InviteStatus,
            createdAt: Instant,
            expiresAt: Instant,
            acceptedBy: MemberId?,
            acceptedAt: Instant?,
        ): PairingInvite =
            PairingInvite(
                id = id,
                code = code,
                inviterId = inviterId,
                status = status,
                createdAt = createdAt,
                expiresAt = expiresAt,
                acceptedBy = acceptedBy,
                acceptedAt = acceptedAt,
            )
    }
}
