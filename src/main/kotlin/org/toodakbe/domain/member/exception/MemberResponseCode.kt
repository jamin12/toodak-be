package org.toodakbe.domain.member.exception

import org.toodakbe.core.response.ResponseCode

/**
 * member 도메인 응답 코드.
 *
 * 도메인 안에 두는 이유: 도메인 모델이 직접 던지는 예외(예: 탈퇴 회원 검증)가 이 enum의 코드를 들고 다닌다.
 * core는 Spring 비의존이라 도메인 순수성은 깨지지 않는다.
 *
 * resultCode 채번은 20x 대 사용.
 */
enum class MemberResponseCode(
    override val httpStatus: Int,
    override val resultCode: Int,
    override val message: String,
) : ResponseCode {
    NOT_ACTIVE(403, 200, "활성 상태의 회원이 아닙니다."),
    WITHDRAWN(403, 201, "탈퇴한 회원입니다."),
    EMAIL_VERIFICATION_REQUIRED(400, 202, "이메일 인증이 필요합니다."),
    NOT_FOUND(500, 203, "회원을 찾을 수 없습니다."),
}
