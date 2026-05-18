package org.toodakbe.adapter.common.restIn.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import org.toodakbe.adapter.common.restIn.dto.CommonResponse
import org.toodakbe.adapter.common.restIn.dto.Header
import org.toodakbe.core.response.CommonResponseCode
import tools.jackson.databind.ObjectMapper

/**
 * 인증은 되었으나 권한이 부족한 요청을 [CommonResponse] 포맷으로 응답한다.
 *
 * 권한 모델(role/scope)이 도입되기 전엔 거의 호출되지 않지만, 미리 일관 포맷을 맞춰둔다.
 */
@Component
class AccessDeniedHandlerImpl(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        response.status = CommonResponseCode.ACCESS_DENIED.httpStatus
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        val body =
            CommonResponse<Nothing?>(
                header =
                    Header(
                        success = false,
                        resultCode = CommonResponseCode.ACCESS_DENIED.resultCode,
                        message = accessDeniedException.message ?: CommonResponseCode.ACCESS_DENIED.message,
                    ),
                data = null,
            )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}
