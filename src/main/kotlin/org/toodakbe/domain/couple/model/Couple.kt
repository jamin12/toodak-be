package org.toodakbe.domain.couple.model

import org.toodakbe.domain.couple.vo.CoupleId
import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

/**
 * 두 회원의 1:1 연결.
 *
 * 도메인 불변식:
 * - [memberAId](초대자)와 [memberBId](합류자)는 생성 출처를 기록하나, 조회·표시는 대칭으로 취급한다.
 * - 한 회원은 동시에 하나의 커플에만 속한다 — 교차 애그리거트 제약이라 앱 레이어/DB에서 강제.
 * - [pairedAt]은 "함께 N일째"의 기준 시각.
 */
class Couple private constructor(
    val id: CoupleId,
    val memberAId: MemberId,
    val memberBId: MemberId,
    val pairedAt: Instant,
) {
    init {
        require(memberAId != memberBId) { "자기 자신과는 연결할 수 없습니다" }
    }

    fun contains(memberId: MemberId): Boolean = memberId == memberAId || memberId == memberBId

    /**
     * 주어진 회원의 상대(파트너)를 반환한다. 마이 헤더의 "나 · 파트너" 계산에 사용.
     */
    fun partnerOf(memberId: MemberId): MemberId {
        require(contains(memberId)) { "이 커플에 속하지 않은 회원입니다: $memberId" }
        return if (memberId == memberAId) memberBId else memberAId
    }

    companion object {
        fun create(
            inviterId: MemberId,
            joinerId: MemberId,
            now: Instant,
        ): Couple =
            Couple(
                id = CoupleId.generate(),
                memberAId = inviterId,
                memberBId = joinerId,
                pairedAt = now,
            )

        /**
         * 영속화 어댑터에서 도메인 모델을 복원할 때 사용.
         */
        fun restore(
            id: CoupleId,
            memberAId: MemberId,
            memberBId: MemberId,
            pairedAt: Instant,
        ): Couple =
            Couple(
                id = id,
                memberAId = memberAId,
                memberBId = memberBId,
                pairedAt = pairedAt,
            )
    }
}
