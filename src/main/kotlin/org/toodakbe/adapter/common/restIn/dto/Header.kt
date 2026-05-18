package org.toodakbe.adapter.common.restIn.dto

/**
 * API 공통 응답 헤더. 모든 컨트롤러 응답이 동일한 메타정보 구조를 갖도록 통일한다.
 */
data class Header(
    val success: Boolean,
    val resultCode: Int,
    val message: String = "",
    val detail: String? = null,
)
