package org.toodakbe.application.auth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.toodakbe.application.auth.dto.LoginWithGoogleCommand
import org.toodakbe.application.auth.dto.TokenPairResult
import org.toodakbe.application.auth.port.inbound.LoginWithGoogleInPort
import org.toodakbe.application.auth.port.outbound.GoogleOAuthOutPort
import org.toodakbe.application.auth.port.outbound.JwtOutPort
import org.toodakbe.application.auth.port.outbound.RefreshTokenOutPort
import org.toodakbe.application.member.exception.MemberNotFoundException
import org.toodakbe.application.member.port.outbound.MemberOutPort
import org.toodakbe.application.member.port.outbound.SocialIdentityOutPort
import org.toodakbe.domain.auth.model.IssuedRefreshToken
import org.toodakbe.domain.auth.model.RefreshToken
import org.toodakbe.domain.auth.model.VerifiedSocialUser
import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.member.enums.MemberStatus
import org.toodakbe.domain.member.exception.MemberNotActiveException
import org.toodakbe.domain.member.model.Member
import org.toodakbe.domain.member.model.SocialIdentity
import org.toodakbe.domain.member.vo.MemberId
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Google ID Token Flow 기반 로그인 UseCase.
 *
 * 분기 정책 (제공자 일반화 — `payload.provider` 로 분기, Google 하드코딩 없음):
 * - SocialIdentity 존재(재로그인): Member 조회 + WITHDRAWN 거부 + 이메일 동기화
 * - SocialIdentity 없음(신규):
 *   - 자동 연결 조건(`payload.provider.trustsEmailVerification && payload.emailVerified`)이면 같은 이메일 ACTIVE Member 찾기
 *   - 없으면 새 Member 생성
 *   - SocialIdentity 연결
 *
 * 인증 토큰 발급(Phase 4):
 * - 같은 디바이스의 기존 활성 Refresh Token 이 있으면 회수 후 신규 발급 (디바이스 단위 단일 세션)
 * - Access Token + Refresh Token 평문을 묶어 반환
 */
@Service
class LoginWithGoogleUseCase(
    private val googleOAuthOutPort: GoogleOAuthOutPort,
    private val memberOutPort: MemberOutPort,
    private val socialIdentityOutPort: SocialIdentityOutPort,
    private val refreshTokenOutPort: RefreshTokenOutPort,
    private val jwtOutPort: JwtOutPort,
    private val clock: Clock,
    @param:Value($$"${jwt.access-ttl}") private val accessTtl: Duration,
    @param:Value($$"${jwt.refresh-ttl}") private val refreshTtl: Duration,
) : LoginWithGoogleInPort {
    @Transactional
    override fun execute(command: LoginWithGoogleCommand): TokenPairResult {
        val payload = googleOAuthOutPort.verifyIdToken(command.idToken)
        val now = clock.instant()

        val existingIdentity = socialIdentityOutPort.findBy(payload.provider, payload.providerUserId)

        val member =
            if (existingIdentity != null) {
                resolveExistingMember(existingIdentity.memberId, payload)
            } else {
                linkOrRegister(payload, now)
            }

        val deviceId = DeviceId(command.deviceId)
        val issuedRefresh = issueRefreshToken(member.id, deviceId, command.deviceLabel, now)

        val accessToken = jwtOutPort.issueAccessToken(member.id, accessTtl)
        return TokenPairResult(
            accessToken = accessToken.value,
            refreshToken = issuedRefresh.plainValue,
            expiresIn = accessTtl.seconds,
        )
    }

    /**
     * 재로그인 경로: 기존 Member 조회 + ACTIVE 검증 + 이메일 동기화.
     */
    private fun resolveExistingMember(
        memberId: MemberId,
        payload: VerifiedSocialUser,
    ): Member {
        val member = memberOutPort.findById(memberId) ?: throw MemberNotFoundException(memberId)
        if (member.status != MemberStatus.ACTIVE) {
            throw MemberNotActiveException(member.id, member.status)
        }
        if (member.email == payload.email) return member
        return memberOutPort.save(member.changeEmail(payload.email))
    }

    /**
     * 신규 경로: 자동 연결 가능하면 같은 이메일 Member에 SocialIdentity 추가, 아니면 새 Member 생성.
     */
    private fun linkOrRegister(
        payload: VerifiedSocialUser,
        now: Instant,
    ): Member {
        val candidate =
            if (payload.provider.trustsEmailVerification && payload.emailVerified) {
                memberOutPort
                    .findByEmail(payload.email)
                    ?.takeIf { it.status == MemberStatus.ACTIVE }
            } else {
                null
            }

        val member = candidate ?: memberOutPort.save(Member.register(payload.email, now))

        val emailVerifiedAt = now.takeIf { payload.emailVerified }
        socialIdentityOutPort.save(
            SocialIdentity.link(
                memberId = member.id,
                provider = payload.provider,
                providerUserId = payload.providerUserId,
                emailVerifiedAt = emailVerifiedAt,
                now = now,
            ),
        )
        return member
    }

    /**
     * 디바이스 단위 단일 세션 정책: 기존 활성 토큰이 있으면 회수 후 신규 발급.
     */
    private fun issueRefreshToken(
        memberId: MemberId,
        deviceId: DeviceId,
        deviceLabel: String?,
        now: Instant,
    ): IssuedRefreshToken {
        refreshTokenOutPort.findActiveBy(memberId, deviceId)?.let { existing ->
            refreshTokenOutPort.save(existing.revoke(now))
        }
        val issued = RefreshToken.issue(memberId, deviceId, deviceLabel, refreshTtl, now)
        refreshTokenOutPort.save(issued.entity)
        return issued
    }
}
