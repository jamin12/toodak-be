package org.toodakbe.application.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.toodakbe.application.auth.dto.LoginWithGoogleCommand
import org.toodakbe.application.auth.port.outbound.GoogleOAuthOutPort
import org.toodakbe.application.auth.port.outbound.JwtOutPort
import org.toodakbe.application.member.port.outbound.MemberOutPort
import org.toodakbe.application.member.port.outbound.SocialIdentityOutPort
import org.toodakbe.domain.auth.model.AccessToken
import org.toodakbe.domain.auth.model.VerifiedSocialUser
import org.toodakbe.domain.member.enums.MemberStatus
import org.toodakbe.domain.member.exception.MemberNotActiveException
import org.toodakbe.domain.member.model.Member
import org.toodakbe.domain.member.model.SocialIdentity
import org.toodakbe.domain.member.vo.Email
import org.toodakbe.domain.member.vo.Provider
import org.toodakbe.domain.member.vo.ProviderUserId
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class LoginWithGoogleUseCaseTest :
    BehaviorSpec({
        val now = Instant.parse("2026-05-15T10:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val accessTtl = Duration.ofMinutes(15)
        val command =
            LoginWithGoogleCommand(
                idToken = "google.id.token",
                deviceId = "device-1",
                deviceLabel = "iPhone 15 Pro",
            )

        fun googlePayload(
            sub: String,
            email: String,
            emailVerified: Boolean = true,
            name: String? = null,
        ): VerifiedSocialUser =
            VerifiedSocialUser.of(
                provider = Provider.GOOGLE,
                providerUserId = ProviderUserId(sub),
                email = Email(email),
                emailVerified = emailVerified,
                name = name,
                picture = null,
            )

        Given("SocialIdentity가 없고 같은 이메일 회원도 없는 신규 사용자 (자동 가입)") {
            val googleOAuthOutPort = mockk<GoogleOAuthOutPort>()
            val memberOutPort = mockk<MemberOutPort>()
            val socialIdentityOutPort = mockk<SocialIdentityOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase =
                LoginWithGoogleUseCase(
                    googleOAuthOutPort = googleOAuthOutPort,
                    memberOutPort = memberOutPort,
                    socialIdentityOutPort = socialIdentityOutPort,
                    jwtOutPort = jwtOutPort,
                    clock = clock,
                    accessTtl = accessTtl,
                )

            val payload = googlePayload("google-sub-1", "new@gmail.com", name = "홍길동")
            every { googleOAuthOutPort.verifyIdToken(command.idToken) } returns payload
            every {
                socialIdentityOutPort.findBy(Provider.GOOGLE, ProviderUserId("google-sub-1"))
            } returns null
            every { memberOutPort.findByEmail(Email("new@gmail.com")) } returns null

            val savedMemberSlot = slot<Member>()
            every { memberOutPort.save(capture(savedMemberSlot)) } answers { savedMemberSlot.captured }

            val savedIdentitySlot = slot<SocialIdentity>()
            every {
                socialIdentityOutPort.save(capture(savedIdentitySlot))
            } answers { savedIdentitySlot.captured }

            every {
                jwtOutPort.issueAccessToken(any(), accessTtl)
            } answers { AccessToken.of("access.jwt.token", now.plus(accessTtl)) }

            When("execute를 호출하면") {
                val result = useCase.execute(command)

                Then("새 ACTIVE Member 가 저장된다") {
                    verify(exactly = 1) { memberOutPort.save(any()) }
                    savedMemberSlot.captured.email shouldBe Email("new@gmail.com")
                    savedMemberSlot.captured.status shouldBe MemberStatus.ACTIVE
                }
                Then("SocialIdentity 가 저장되고 새 Member 에 연결된다") {
                    verify(exactly = 1) { socialIdentityOutPort.save(any()) }
                    savedIdentitySlot.captured.memberId shouldBe savedMemberSlot.captured.id
                    savedIdentitySlot.captured.provider shouldBe Provider.GOOGLE
                    savedIdentitySlot.captured.providerUserId shouldBe ProviderUserId("google-sub-1")
                    savedIdentitySlot.captured.emailVerifiedAt shouldBe now
                }
                Then("Access Token 이 발급된다") {
                    result.accessToken shouldBe "access.jwt.token"
                    result.refreshToken shouldBe null
                    result.expiresIn shouldBe accessTtl.seconds
                }
            }
        }

        Given("SocialIdentity 가 없지만 같은 이메일의 ACTIVE 회원이 존재 (자동 연결)") {
            val googleOAuthOutPort = mockk<GoogleOAuthOutPort>()
            val memberOutPort = mockk<MemberOutPort>()
            val socialIdentityOutPort = mockk<SocialIdentityOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase =
                LoginWithGoogleUseCase(
                    googleOAuthOutPort,
                    memberOutPort,
                    socialIdentityOutPort,
                    jwtOutPort,
                    clock,
                    accessTtl,
                )

            val existingMember = Member.register(Email("user@gmail.com"), now.minusSeconds(86_400))
            val payload = googlePayload("google-sub-2", "user@gmail.com")

            every { googleOAuthOutPort.verifyIdToken(command.idToken) } returns payload
            every {
                socialIdentityOutPort.findBy(Provider.GOOGLE, ProviderUserId("google-sub-2"))
            } returns null
            every { memberOutPort.findByEmail(Email("user@gmail.com")) } returns existingMember

            val savedIdentitySlot = slot<SocialIdentity>()
            every {
                socialIdentityOutPort.save(capture(savedIdentitySlot))
            } answers { savedIdentitySlot.captured }

            every {
                jwtOutPort.issueAccessToken(existingMember.id, accessTtl)
            } returns AccessToken.of("access.jwt.token", now.plus(accessTtl))

            When("execute를 호출하면") {
                val result = useCase.execute(command)

                Then("새 Member 는 생성되지 않는다") {
                    verify(exactly = 0) { memberOutPort.save(any()) }
                }
                Then("기존 Member 에 SocialIdentity 가 연결된다") {
                    savedIdentitySlot.captured.memberId shouldBe existingMember.id
                }
                Then("기존 Member 의 id 로 Access Token 이 발급된다") {
                    result.accessToken shouldBe "access.jwt.token"
                }
            }
        }

        Given("SocialIdentity 가 존재하고 이메일이 같은 재로그인") {
            val googleOAuthOutPort = mockk<GoogleOAuthOutPort>()
            val memberOutPort = mockk<MemberOutPort>()
            val socialIdentityOutPort = mockk<SocialIdentityOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase =
                LoginWithGoogleUseCase(
                    googleOAuthOutPort,
                    memberOutPort,
                    socialIdentityOutPort,
                    jwtOutPort,
                    clock,
                    accessTtl,
                )

            val member = Member.register(Email("user@gmail.com"), now.minusSeconds(86_400))
            val identity =
                SocialIdentity.link(
                    memberId = member.id,
                    provider = Provider.GOOGLE,
                    providerUserId = ProviderUserId("google-sub-3"),
                    emailVerifiedAt = now.minusSeconds(86_400),
                    now = now.minusSeconds(86_400),
                )
            val payload = googlePayload("google-sub-3", "user@gmail.com")

            every { googleOAuthOutPort.verifyIdToken(command.idToken) } returns payload
            every {
                socialIdentityOutPort.findBy(Provider.GOOGLE, ProviderUserId("google-sub-3"))
            } returns identity
            every { memberOutPort.findById(member.id) } returns member
            every {
                jwtOutPort.issueAccessToken(member.id, accessTtl)
            } returns AccessToken.of("access.jwt.token", now.plus(accessTtl))

            When("execute를 호출하면") {
                val result = useCase.execute(command)

                Then("Member 저장은 호출되지 않는다 (이메일 동기화 불필요)") {
                    verify(exactly = 0) { memberOutPort.save(any()) }
                }
                Then("새 SocialIdentity 도 생성되지 않는다") {
                    verify(exactly = 0) { socialIdentityOutPort.save(any()) }
                }
                Then("Access Token 이 발급된다") {
                    result.accessToken shouldBe "access.jwt.token"
                }
            }
        }

        Given("SocialIdentity 가 존재하고 Google 응답의 이메일이 변경되었을 때 (재로그인 + 이메일 동기화)") {
            val googleOAuthOutPort = mockk<GoogleOAuthOutPort>()
            val memberOutPort = mockk<MemberOutPort>()
            val socialIdentityOutPort = mockk<SocialIdentityOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase =
                LoginWithGoogleUseCase(
                    googleOAuthOutPort,
                    memberOutPort,
                    socialIdentityOutPort,
                    jwtOutPort,
                    clock,
                    accessTtl,
                )

            val member = Member.register(Email("old@gmail.com"), now.minusSeconds(86_400))
            val identity =
                SocialIdentity.link(
                    member.id,
                    Provider.GOOGLE,
                    ProviderUserId("google-sub-4"),
                    now.minusSeconds(86_400),
                    now.minusSeconds(86_400),
                )
            val payload = googlePayload("google-sub-4", "new@gmail.com")

            every { googleOAuthOutPort.verifyIdToken(command.idToken) } returns payload
            every {
                socialIdentityOutPort.findBy(Provider.GOOGLE, ProviderUserId("google-sub-4"))
            } returns identity
            every { memberOutPort.findById(member.id) } returns member

            val savedSlot = slot<Member>()
            every { memberOutPort.save(capture(savedSlot)) } answers { savedSlot.captured }
            every {
                jwtOutPort.issueAccessToken(member.id, accessTtl)
            } returns AccessToken.of("access.jwt.token", now.plus(accessTtl))

            When("execute를 호출하면") {
                useCase.execute(command)

                Then("새 이메일로 Member 가 저장된다") {
                    verify(exactly = 1) { memberOutPort.save(any()) }
                    savedSlot.captured.email shouldBe Email("new@gmail.com")
                    savedSlot.captured.id shouldBe member.id
                }
            }
        }

        Given("SocialIdentity 가 가리키는 Member 가 WITHDRAWN 상태") {
            val googleOAuthOutPort = mockk<GoogleOAuthOutPort>()
            val memberOutPort = mockk<MemberOutPort>()
            val socialIdentityOutPort = mockk<SocialIdentityOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase =
                LoginWithGoogleUseCase(
                    googleOAuthOutPort,
                    memberOutPort,
                    socialIdentityOutPort,
                    jwtOutPort,
                    clock,
                    accessTtl,
                )

            val withdrawn = Member.register(Email("user@gmail.com"), now.minusSeconds(86_400)).withdraw(now.minusSeconds(60))
            val identity =
                SocialIdentity.link(
                    withdrawn.id,
                    Provider.GOOGLE,
                    ProviderUserId("google-sub-5"),
                    now.minusSeconds(86_400),
                    now.minusSeconds(86_400),
                )
            val payload = googlePayload("google-sub-5", "user@gmail.com")

            every { googleOAuthOutPort.verifyIdToken(command.idToken) } returns payload
            every {
                socialIdentityOutPort.findBy(Provider.GOOGLE, ProviderUserId("google-sub-5"))
            } returns identity
            every { memberOutPort.findById(withdrawn.id) } returns withdrawn

            When("execute를 호출하면") {
                Then("MemberNotActiveException 이 발생한다") {
                    shouldThrow<MemberNotActiveException> { useCase.execute(command) }
                    verify(exactly = 0) { jwtOutPort.issueAccessToken(any(), any()) }
                }
            }
        }
    })
