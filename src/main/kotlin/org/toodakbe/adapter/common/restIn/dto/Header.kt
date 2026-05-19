package org.toodakbe.adapter.common.restIn.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "API 공통 응답 헤더 (메타정보)")
data class Header(
    @field:Schema(description = "요청 처리 성공 여부", example = "true")
    val success: Boolean,
    @field:Schema(description = "도메인별 응답 코드 (CommonResponseCode / 도메인 ResponseCode 의 resultCode)", example = "101")
    val resultCode: Int,
    @field:Schema(description = "응답 메시지", example = "성공")
    val message: String = "",
    @field:Schema(description = "에러 상세 (실패 응답에서만 채워질 수 있음)", nullable = true)
    val detail: String? = null,
)
