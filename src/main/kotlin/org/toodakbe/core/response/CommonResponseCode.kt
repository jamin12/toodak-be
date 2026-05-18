package org.toodakbe.core.response

/**
 * 전역 공통 응답 코드. 도메인 특화 코드는 도메인 패키지의 별도 enum이 [ResponseCode]를 구현한다.
 *
 * 5xx는 외부 응답 시 4xx로 강제 변환된다 (`GlobalExceptionHandler.coerceTo4xx`).
 */
enum class CommonResponseCode(
    override val httpStatus: Int,
    override val resultCode: Int,
    override val message: String,
) : ResponseCode {
    SUCCESS(200, 101, "성공"),
    NOT_FOUND(404, 102, "리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(401, 103, "인증이 필요합니다."),
    ACCESS_DENIED(403, 104, "접근 권한이 없습니다."),
    BAD_REQUEST(400, 106, "잘못된 요청입니다."),
    INTERNAL_ERROR(500, 107, "내부 오류가 발생했습니다."),
    VALIDATION(400, 108, "입력값이 유효하지 않습니다."),
}
