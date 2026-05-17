package org.toodakbe.domain.common.exception

/**
 * 도메인 레이어에서 발생하는 비즈니스 규칙 위반 예외의 기반.
 *
 * 도메인 모델 내부 불변식이 깨질 때 발생한다. 외부 라이브러리/프레임워크에 의존하지 않는다.
 * 어댑터(REST 등)는 이 예외를 적절한 응답으로 매핑한다.
 */
abstract class DomainException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
