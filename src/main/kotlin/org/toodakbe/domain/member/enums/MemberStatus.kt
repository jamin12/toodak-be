package org.toodakbe.domain.member.enums

/**
 * 회원 상태.
 *
 * - [ACTIVE]: 정상 회원. 로그인 가능.
 * - [WITHDRAWN]: 탈퇴 처리된 회원 (soft delete). 로그인 불가.
 *
 * 1차 스코프에선 탈퇴 기능을 제공하지 않으나, 상태 머신은 미리 정의해 둔다.
 * 정지(SUSPENDED) 등 운영 상태는 어드민 기능이 추가될 때 확장한다.
 */
enum class MemberStatus {
    ACTIVE,
    WITHDRAWN,
}
