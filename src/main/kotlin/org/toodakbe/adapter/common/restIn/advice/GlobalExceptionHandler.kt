package org.toodakbe.adapter.common.restIn.advice

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.toodakbe.adapter.common.restIn.dto.CommonResponse
import org.toodakbe.adapter.common.restIn.dto.Header
import org.toodakbe.core.exception.BaseRuntimeException
import org.toodakbe.core.response.CommonResponseCode
import org.toodakbe.core.response.ResponseCode

/**
 * 모든 컨트롤러 예외를 [CommonResponse] 포맷으로 통일하는 글로벌 핸들러.
 *
 * 처리 카테고리:
 * 1. Spring 입력 검증 실패 (`@Valid`, `@Validated`) → [CommonResponseCode.VALIDATION]
 * 2. Spring 요청 파싱/필수 파라미터 누락 → [CommonResponseCode.BAD_REQUEST]
 * 3. [BaseRuntimeException] (도메인/애플리케이션 비즈니스 예외, 자기 [ResponseCode]를 들고 다님)
 * 4. 그 외 모든 예외 → [CommonResponseCode.INTERNAL_ERROR]
 *
 * 정책: 5xx 상태는 외부에 그대로 노출하지 않고 4xx로 강제 변환한다.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<CommonResponse<Nothing?>> {
        val first = ex.bindingResult.allErrors.firstOrNull()
        val message = first?.defaultMessage ?: CommonResponseCode.VALIDATION.message
        log.warn("MethodArgumentNotValidException: {}", message, ex)
        return errorResponse(CommonResponseCode.VALIDATION, message)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(ex: BindException): ResponseEntity<CommonResponse<Nothing?>> {
        val first = ex.bindingResult.allErrors.firstOrNull()
        val message = first?.defaultMessage ?: CommonResponseCode.VALIDATION.message
        log.warn("BindException: {}", message, ex)
        return errorResponse(CommonResponseCode.VALIDATION, message)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<CommonResponse<Nothing?>> {
        val message = ex.constraintViolations.firstOrNull()?.message ?: CommonResponseCode.VALIDATION.message
        log.warn("ConstraintViolationException: {}", message, ex)
        return errorResponse(CommonResponseCode.VALIDATION, message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<CommonResponse<Nothing?>> {
        log.warn("HttpMessageNotReadableException", ex)
        return errorResponse(CommonResponseCode.BAD_REQUEST, "요청 본문을 읽을 수 없습니다.")
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException): ResponseEntity<CommonResponse<Nothing?>> {
        val message = "필수 요청 파라미터가 누락되었습니다: ${ex.parameterName}"
        log.warn("MissingServletRequestParameterException: {}", message, ex)
        return errorResponse(CommonResponseCode.BAD_REQUEST, message)
    }

    @ExceptionHandler(BaseRuntimeException::class)
    fun handleBase(ex: BaseRuntimeException): ResponseEntity<CommonResponse<Nothing?>> {
        log.warn("BaseRuntimeException [{}]: {}", ex.responseCode.resultCode, ex.detailMessage, ex)
        return errorResponse(ex.responseCode, ex.responseCode.message, ex.detailMessage)
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception): ResponseEntity<CommonResponse<Nothing?>> {
        log.error("Unhandled exception", ex)
        return errorResponse(CommonResponseCode.INTERNAL_ERROR, CommonResponseCode.INTERNAL_ERROR.message)
    }

    private fun errorResponse(
        code: ResponseCode,
        message: String,
        detail: String? = null,
    ): ResponseEntity<CommonResponse<Nothing?>> {
        val status = coerceTo4xx(code.httpStatus)
        val body =
            CommonResponse<Nothing?>(
                header =
                    Header(
                        success = false,
                        resultCode = code.resultCode,
                        message = message,
                        detail = detail,
                    ),
                data = null,
            )
        return ResponseEntity
            .status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
    }

    /** 5xx 응답을 외부에 그대로 노출하지 않고 400으로 변환한다. */
    private fun coerceTo4xx(status: Int): Int = if (status in 500..599) 400 else status
}
