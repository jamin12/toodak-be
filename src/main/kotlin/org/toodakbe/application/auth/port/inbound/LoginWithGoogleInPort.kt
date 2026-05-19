package org.toodakbe.application.auth.port.inbound

import org.toodakbe.application.auth.dto.LoginWithGoogleCommand
import org.toodakbe.application.auth.dto.TokenPairResult

/**
 * Google ID Token Flow 로그인 InPort.
 *
 * 신규 가입/자동 연결/재로그인 분기를 흡수하고 TokenPair를 반환한다.
 */
interface LoginWithGoogleInPort {
    fun execute(command: LoginWithGoogleCommand): TokenPairResult
}
