package org.toodakbe.core.exception

import org.toodakbe.core.response.ResponseCode

/**
 * 비즈니스 예외의 단일 베이스. 예외가 자기 [ResponseCode]를 들고 다니므로
 * `GlobalExceptionHandler`는 `@ExceptionHandler(BaseRuntimeException::class)` 하나로 모든 비즈니스 예외를 응답으로 변환한다.
 */
abstract class BaseRuntimeException(
    val responseCode: ResponseCode,
    val detailMessage: String? = null,
    cause: Throwable? = null,
) : RuntimeException(responseCode.message, cause)
