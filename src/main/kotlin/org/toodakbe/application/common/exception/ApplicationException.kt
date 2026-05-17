package org.toodakbe.application.common.exception

/**
 * 애플리케이션 레이어(UseCase) 또는 어댑터-포트 경계에서 발생하는 예외의 기반.
 *
 * 인증/인가, 외부 시스템 검증 실패, 입력 유효성 등 도메인 불변식 외의 사유로 발생하는 예외를
 * 표현한다. 어댑터(REST 등)에서 4xx/5xx 응답으로 매핑한다.
 */
abstract class ApplicationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
