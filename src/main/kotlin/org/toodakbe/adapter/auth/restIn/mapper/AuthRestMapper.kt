package org.toodakbe.adapter.auth.restIn.mapper

import org.mapstruct.Mapper
import org.toodakbe.adapter.auth.restIn.dto.LoginWithGoogleRequest
import org.toodakbe.adapter.auth.restIn.dto.TokenPairResponse
import org.toodakbe.application.auth.dto.LoginWithGoogleCommand
import org.toodakbe.application.auth.dto.TokenPairResult

/**
 * Auth REST 매퍼.
 *
 * Request → Command, Result → Response 단순 1:1 매핑이므로 MapStruct 인터페이스만 사용한다.
 * 필드 조합/가공이 필요해지면 같은 파일에 확장함수를 함께 둔다 (REST 매퍼 패턴).
 */
@Mapper(componentModel = "spring")
interface AuthRestMapper {
    fun toCommand(request: LoginWithGoogleRequest): LoginWithGoogleCommand

    fun toResponse(result: TokenPairResult): TokenPairResponse
}
