package org.toodakbe.core.response

/**
 * 응답 코드 추상화. 공통/도메인별 enum이 이 인터페이스를 구현한다.
 *
 * [httpStatus]를 `Int`로 둔 이유: core 패키지가 Spring 의존을 피해 도메인 레이어에서도 사용 가능하도록.
 * 어댑터에서 필요하면 `HttpStatus.valueOf(...)`로 변환한다.
 */
interface ResponseCode {
    val httpStatus: Int
    val resultCode: Int
    val message: String
}
