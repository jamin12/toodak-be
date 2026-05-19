package org.toodakbe.application.member.exception

import org.toodakbe.core.exception.BaseRuntimeException
import org.toodakbe.domain.member.exception.MemberResponseCode
import org.toodakbe.domain.member.vo.MemberId

/**
 * Member 가 존재해야 하는 상황에서 조회 실패 시 발생.
 *
 * 대표 케이스: SocialIdentity 가 가리키는 `memberId` 로 Member 를 조회했는데 없는 경우 —
 * `(provider, providerUserId)` UNIQUE + FK 가 있어 정상 흐름에서는 발생 X. 발생하면 데이터 정합성 문제.
 */
class MemberNotFoundException(
    memberId: MemberId,
) : BaseRuntimeException(
        responseCode = MemberResponseCode.NOT_FOUND,
        detailMessage = "회원(${memberId.value})을 찾을 수 없습니다.",
    )
