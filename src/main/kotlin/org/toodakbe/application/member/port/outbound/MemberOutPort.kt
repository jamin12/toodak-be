package org.toodakbe.application.member.port.outbound

import org.toodakbe.domain.member.model.Member
import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.MemberId

/**
 * member 테이블 영속성 OutPort.
 */
interface MemberOutPort {
    /**
     * 회원을 신규 저장하거나 기존 회원의 변경 사항을 반영한다.
     */
    fun save(member: Member): Member

    fun findById(memberId: MemberId): Member?

    /**
     * 이메일로 회원을 조회한다. 이메일은 UNIQUE 가 아니므로 동일 이메일의 회원이
     * 여러 명일 수 있으나, 자동 계정 연결 정책상 가장 먼저 발견된 ACTIVE 회원을 반환한다.
     */
    fun findByEmail(email: Email): Member?
}
