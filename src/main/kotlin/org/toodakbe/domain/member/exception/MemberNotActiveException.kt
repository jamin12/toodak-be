package org.toodakbe.domain.member.exception

import org.toodakbe.core.exception.BaseRuntimeException
import org.toodakbe.domain.member.enums.MemberStatus
import org.toodakbe.domain.member.vo.MemberId

/**
 * ACTIVE 상태가 아닌 회원에 대해 변경 작업을 시도한 경우 발생.
 */
class MemberNotActiveException(
    memberId: MemberId,
    status: MemberStatus,
) : BaseRuntimeException(
        responseCode = MemberResponseCode.NOT_ACTIVE,
        detailMessage = "회원 ${memberId.value}은 상태가 $status 이므로 작업할 수 없습니다",
    )
