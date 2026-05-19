package org.toodakbe.adapter.common.restIn.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.toodakbe.core.response.CommonResponseCode

/**
 * 토닥 API 공통 응답 wrapper. 모든 응답이 `{ header, data }` 구조로 통일된다.
 *
 * 성공: [ok] 팩토리. 실패: `GlobalExceptionHandler`가 헤더를 채워 생성.
 */
@Schema(description = "토닥 API 공통 응답 wrapper")
data class CommonResponse<T>(
    @field:Schema(description = "응답 메타정보")
    val header: Header,
    @field:Schema(description = "응답 본문 (실패 시 null)", nullable = true)
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
