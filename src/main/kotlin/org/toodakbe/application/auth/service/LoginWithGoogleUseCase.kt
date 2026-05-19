package org.toodakbe.application.auth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.toodakbe.application.auth.dto.LoginWithGoogleCommand
import org.toodakbe.application.auth.dto.TokenPairResult
import org.toodakbe.application.auth.port.inbound.LoginWithGoogleInPort
import org.toodakbe.application.auth.port.outbound.GoogleOAuthOutPort
import org.toodakbe.application.auth.port.outbound.JwtOutPort
import org.toodakbe.application.member.exception.MemberNotFoundException
import org.toodakbe.application.member.port.outbound.MemberOutPort
import org.toodakbe.application.member.port.outbound.SocialIdentityOutPort
import org.toodakbe.domain.auth.model.VerifiedSocialUser
import org.toodakbe.domain.member.enums.MemberStatus
import org.toodakbe.domain.member.exception.MemberNotActiveException
import org.toodakbe.domain.member.model.Member
import org.toodakbe.domain.member.model.SocialIdentity
import org.toodakbe.domain.member.vo.MemberId
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Google ID Token Flow 기반 로그인 UseCase (Phase 3 1차 — Access Token만).
 *
 * 분기 정책 (제공자 일반화 — `payload.provider` 로 분기, Google 하드코딩 없음):
 * - SocialIdentity 존재(재로그인): Member 조회 + WITHDRAWN 거부 + 이메일 동기화
 * - SocialIdentity 없음(신규):
 *   - 자동 연결 조건(`payload.provider.trustsEmailVerification && payload.emailVerified`)이면 같은 이메일 ACTIVE Member 찾기
 *   - 없으면 새 Member 생성
 *   - SocialIdentity 연결
 *
 * RefreshToken 발급은 Phase 4 에서 추가된다 — 현재는 `null`을 반환.
 */
@Service
class LoginWithGoogleUseCase(
    private val googleOAuthOutPort: GoogleOAuthOutPort,
    private val memberOutPort: MemberOutPort,
    private val socialIdentityOutPort: SocialIdentityOutPort,
    private val jwtOutPort: JwtOutPort,
    private val clock: Clock,
    @param:Value($$"${jwt.access-ttl}") private val accessTtl: Duration,
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

        val accessToken = jwtOutPort.issueAccessToken(member.id, accessTtl)
        return TokenPairResult(
            accessToken = accessToken.value,
            refreshToken = null,
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
}
