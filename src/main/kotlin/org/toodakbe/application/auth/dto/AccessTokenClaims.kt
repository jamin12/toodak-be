package org.toodakbe.application.auth.dto

import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

/**
 * Access Token에서 추출한 검증된 클레임.
 *
 * `JwtOutPort.parseAccessToken`이 반환하는 결과 DTO.
 * 인증 필터/보호된 컨트롤러에서 `memberId`를 통해 요청 사용자를 식별한다.
 *
 * @property memberId 토큰 subject로부터 복원된 회원 식별자
 * @property expiresAt 토큰 만료 시각 (`exp` claim)
 */
data class AccessTokenClaims(
    val memberId: MemberId,
    val expiresAt: Instant,
)
