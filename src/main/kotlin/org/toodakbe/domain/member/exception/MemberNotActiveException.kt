package org.toodakbe.domain.member.exception

import org.toodakbe.domain.common.exception.DomainException
import org.toodakbe.domain.member.enums.MemberStatus
import org.toodakbe.domain.member.vo.MemberId

/**
 * ACTIVE 상태가 아닌 회원에게 변경 작업을 시도한 경우.
 *
 * 예: 탈퇴된 회원의 이메일을 변경하려는 시도.
 */
class MemberNotActiveException(
    memberId: MemberId,
    status: MemberStatus,
) : DomainException("회원 ${memberId.value}은 상태가 $status 이므로 작업할 수 없습니다")
