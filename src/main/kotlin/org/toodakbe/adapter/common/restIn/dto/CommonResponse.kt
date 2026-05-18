package org.toodakbe.adapter.common.restIn.dto

import org.toodakbe.core.response.CommonResponseCode

/**
 * 토닥 API 공통 응답 wrapper. 모든 응답이 `{ header, data }` 구조로 통일된다.
 *
 * 성공: [ok] 팩토리. 실패: `GlobalExceptionHandler`가 헤더를 채워 생성.
 */
data class CommonResponse<T>(
    val header: Header,
    val data: T? = null,
) {
    companion object {
        fun ok(): CommonResponse<Unit?> = ok<Unit?>(null)

        fun <U> ok(data: U?): CommonResponse<U?> =
            CommonResponse(
                header =
                    Header(
                        success = true,
                        resultCode = CommonResponseCode.SUCCESS.resultCode,
                        message = CommonResponseCode.SUCCESS.message,
                    ),
                data = data,
            )
    }
}
