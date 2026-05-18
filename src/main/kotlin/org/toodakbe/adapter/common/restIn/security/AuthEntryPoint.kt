package org.toodakbe.adapter.common.restIn.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.toodakbe.adapter.common.restIn.dto.CommonResponse
import org.toodakbe.adapter.common.restIn.dto.Header
import org.toodakbe.core.response.CommonResponseCode
import tools.jackson.databind.ObjectMapper

/**
 * Spring Security가 미인증 요청을 만나면 호출되는 EntryPoint.
 *
 * `GlobalExceptionHandler`가 다루지 못하는 영역(필터 단계의 인증 실패)을 [CommonResponse] 포맷으로 통일한다.
 */
@Component
class AuthEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.status = CommonResponseCode.UNAUTHORIZED.httpStatus
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()

        val body =
            CommonResponse<Nothing?>(
                header =
                    Header(
                        success = false,
                        resultCode = CommonResponseCode.UNAUTHORIZED.resultCode,
                        message = authException.message ?: CommonResponseCode.UNAUTHORIZED.message,
                    ),
                data = null,
            )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}
