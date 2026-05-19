package org.toodakbe.application.auth.port.inbound

import org.toodakbe.application.auth.dto.LogoutCommand

/**
 * 로그아웃 InPort — 디바이스 단위 Refresh Token 폐기.
 *
 * 멱등 — 토큰이 존재하지 않거나 이미 폐기된 상태여도 예외 없이 정상 종료.
 */
interface LogoutInPort {
    fun execute(command: LogoutCommand)
}
