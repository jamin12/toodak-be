package org.toodakbe.application.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.toodakbe.application.auth.dto.RefreshAccessTokenCommand
import org.toodakbe.application.auth.exception.InvalidRefreshTokenException
import org.toodakbe.application.auth.exception.RefreshTokenDeviceMismatchException
import org.toodakbe.application.auth.exception.RefreshTokenExpiredException
import org.toodakbe.application.auth.exception.RefreshTokenReuseDetectedException
import org.toodakbe.application.auth.port.outbound.JwtOutPort
import org.toodakbe.application.auth.port.outbound.RefreshTokenOutPort
import org.toodakbe.domain.auth.model.AccessToken
import org.toodakbe.domain.auth.model.IssuedRefreshToken
import org.toodakbe.domain.auth.model.RefreshToken
import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.member.vo.MemberId
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class RefreshAccessTokenUseCaseTest :
    BehaviorSpec({
        val now = Instant.parse("2026-05-15T10:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val accessTtl = Duration.ofMinutes(15)
        val refreshTtl = Duration.ofDays(30)

        fun newUseCase(
            refreshTokenOutPort: RefreshTokenOutPort,
            jwtOutPort: JwtOutPort,
        ) = RefreshAccessTokenUseCase(
            refreshTokenOutPort = refreshTokenOutPort,
            jwtOutPort = jwtOutPort,
            clock = clock,
            accessTtl = accessTtl,
            refreshTtl = refreshTtl,
        )

        fun issueAt(
            memberId: MemberId,
            deviceId: DeviceId,
            issuedAt: Instant,
        ): IssuedRefreshToken = RefreshToken.issue(memberId, deviceId, "label", refreshTtl, issuedAt)

        Given("유효한 활성 RefreshToken 으로 회전 요청") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase = newUseCase(refreshTokenOutPort, jwtOutPort)

            val memberId = MemberId.generate()
            val deviceId = DeviceId("device-1")
            val current = issueAt(memberId, deviceId, now.minusSeconds(3600))
            val command = RefreshAccessTokenCommand(refreshToken = current.plainValue, deviceId = "device-1")

            every { refreshTokenOutPort.findByHash(current.entity.tokenHash) } returns current.entity

            val savedSlots = mutableListOf<RefreshToken>()
            every { refreshTokenOutPort.save(capture(savedSlots)) } answers { firstArg() }

            every {
                jwtOutPort.issueAccessToken(memberId, accessTtl)
            } returns AccessToken.of("new.access.jwt", now.plus(accessTtl))

            When("execute 를 호출하면") {
                val result = useCase.execute(command)

                Then("기존 토큰이 revoke 되고 회전된 새 토큰이 저장된다 (총 2회 save)") {
                    verify(exactly = 2) { refreshTokenOutPort.save(any()) }
                    savedSlots[0].id shouldBe current.entity.id
                    savedSlots[0].isRevoked() shouldBe true
                    savedSlots[1].rotatedFromId shouldBe current.entity.id
                    savedSlots[1].isRevoked() shouldBe false
                    savedSlots[1].memberId shouldBe memberId
                    savedSlots[1].deviceId shouldBe deviceId
                    savedSlots[1].expiresAt shouldBe now.plus(refreshTtl)
                }
                Then("Access/Refresh 응답이 새 값으로 반환된다") {
                    result.accessToken shouldBe "new.access.jwt"
                    result.expiresIn shouldBe accessTtl.seconds
                    RefreshToken.hashOfPlain(result.refreshToken) shouldBe savedSlots[1].tokenHash
                    result.refreshToken.length shouldBe 43
                }
            }
        }

        Given("DB 에 존재하지 않는 RefreshToken") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase = newUseCase(refreshTokenOutPort, jwtOutPort)

            val command = RefreshAccessTokenCommand(refreshToken = "unknown-plain-value", deviceId = "device-1")
            every {
                refreshTokenOutPort.findByHash(RefreshToken.hashOfPlain("unknown-plain-value"))
            } returns null

            When("execute 를 호출하면") {
                Then("InvalidRefreshTokenException 이 발생한다") {
                    shouldThrow<InvalidRefreshTokenException> { useCase.execute(command) }
                    verify(exactly = 0) { refreshTokenOutPort.save(any()) }
                    verify(exactly = 0) { jwtOutPort.issueAccessToken(any(), any()) }
                }
            }
        }

        Given("이미 revoke 된 RefreshToken 으로 회전 시도 (탈취 감지)") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase = newUseCase(refreshTokenOutPort, jwtOutPort)

            val memberId = MemberId.generate()
            val deviceId = DeviceId("device-1")
            val issued = issueAt(memberId, deviceId, now.minusSeconds(3600))
            val revoked = issued.entity.revoke(now.minusSeconds(60))
            val command = RefreshAccessTokenCommand(refreshToken = issued.plainValue, deviceId = "device-1")

            every { refreshTokenOutPort.findByHash(issued.entity.tokenHash) } returns revoked
            every { refreshTokenOutPort.revokeAllByMember(memberId, now) } returns 3

            When("execute 를 호출하면") {
                Then("회원의 모든 활성 토큰을 회수한 뒤 RefreshTokenReuseDetectedException 이 발생한다") {
                    shouldThrow<RefreshTokenReuseDetectedException> { useCase.execute(command) }
                    verify(exactly = 1) { refreshTokenOutPort.revokeAllByMember(memberId, now) }
                    verify(exactly = 0) { refreshTokenOutPort.save(any()) }
                    verify(exactly = 0) { jwtOutPort.issueAccessToken(any(), any()) }
                }
            }
        }

        Given("만료된 RefreshToken") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase = newUseCase(refreshTokenOutPort, jwtOutPort)

            val memberId = MemberId.generate()
            val deviceId = DeviceId("device-1")
            val issuedAt = now.minus(refreshTtl).minusSeconds(60)
            val expired = issueAt(memberId, deviceId, issuedAt)
            val command = RefreshAccessTokenCommand(refreshToken = expired.plainValue, deviceId = "device-1")

            every { refreshTokenOutPort.findByHash(expired.entity.tokenHash) } returns expired.entity

            When("execute 를 호출하면") {
                Then("RefreshTokenExpiredException 이 발생한다") {
                    shouldThrow<RefreshTokenExpiredException> { useCase.execute(command) }
                    verify(exactly = 0) { refreshTokenOutPort.save(any()) }
                    verify(exactly = 0) { refreshTokenOutPort.revokeAllByMember(any(), any()) }
                    verify(exactly = 0) { jwtOutPort.issueAccessToken(any(), any()) }
                }
            }
        }

        Given("디바이스 ID 가 불일치하는 RefreshToken") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase = newUseCase(refreshTokenOutPort, jwtOutPort)

            val memberId = MemberId.generate()
            val issued = issueAt(memberId, DeviceId("device-1"), now.minusSeconds(3600))
            val command = RefreshAccessTokenCommand(refreshToken = issued.plainValue, deviceId = "device-other")

            every { refreshTokenOutPort.findByHash(issued.entity.tokenHash) } returns issued.entity

            When("execute 를 호출하면") {
                Then("RefreshTokenDeviceMismatchException 이 발생한다") {
                    shouldThrow<RefreshTokenDeviceMismatchException> { useCase.execute(command) }
                    verify(exactly = 0) { refreshTokenOutPort.save(any()) }
                    verify(exactly = 0) { jwtOutPort.issueAccessToken(any(), any()) }
                }
            }
        }

        Given("정상 회전 후 결과 검증 — save 순서 확인") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val jwtOutPort = mockk<JwtOutPort>()
            val useCase = newUseCase(refreshTokenOutPort, jwtOutPort)

            val memberId = MemberId.generate()
            val deviceId = DeviceId("device-1")
            val current = issueAt(memberId, deviceId, now.minusSeconds(3600))
            val command = RefreshAccessTokenCommand(refreshToken = current.plainValue, deviceId = "device-1")

            every { refreshTokenOutPort.findByHash(current.entity.tokenHash) } returns current.entity
            val sink = slot<RefreshToken>()
            every { refreshTokenOutPort.save(capture(sink)) } answers { firstArg() }
            every {
                jwtOutPort.issueAccessToken(memberId, accessTtl)
            } returns AccessToken.of("new.jwt", now.plus(accessTtl))

            When("execute 를 호출하면") {
                useCase.execute(command)

                Then("마지막 save 가 신규 회전 토큰이다") {
                    sink.captured.rotatedFromId shouldBe current.entity.id
                }
            }
        }
    })
