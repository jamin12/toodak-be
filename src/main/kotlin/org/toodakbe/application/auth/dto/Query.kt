package org.toodakbe.application.auth.dto

import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

/**
 * Access Token 에서 추출한 검증된 클레임.
 *
 * `JwtOutPort.parseAccessToken` 의 반환 타입.
 * 도메인 모델로 표현하기엔 외부 의존(JWT 라이브러리)의 1차 산출물에 가까워 Query DTO 로 둔다.
 *
 * @property memberId 토큰 subject 로부터 복원된 회원 식별자
 * @property expiresAt 토큰 만료 시각 (`exp` claim)
 */
data class AccessTokenClaims(
    val memberId: MemberId,
    val expiresAt: Instant,
)
