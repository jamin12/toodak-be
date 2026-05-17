package org.toodakbe.domain.auth.model

/**
 * RefreshToken 발급 결과.
 *
 * - [entity]: DB에 저장될 RefreshToken (해시만 보유, 평문 X).
 * - [plainValue]: 클라이언트 응답에 한 번만 전달할 평문 토큰. DB엔 저장하지 않는다.
 *
 * 발급/회전 직후에만 만들어지는 일회성 객체.
 */
class IssuedRefreshToken private constructor(
    val entity: RefreshToken,
    val plainValue: String,
) {
    companion object {
        fun of(
            entity: RefreshToken,
            plainValue: String,
        ): IssuedRefreshToken {
            return IssuedRefreshToken(entity = entity, plainValue = plainValue)
        }
    }
}
