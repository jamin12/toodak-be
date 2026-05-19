package org.toodakbe.application.auth.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.toodakbe.application.auth.dto.LogoutCommand
import org.toodakbe.application.auth.port.outbound.RefreshTokenOutPort
import org.toodakbe.domain.auth.model.RefreshToken
import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.member.vo.MemberId
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class LogoutUseCaseTest :
    BehaviorSpec({
        val now = Instant.parse("2026-05-15T10:00:00Z")
        val clock = Clock.fixed(now, ZoneOffset.UTC)
        val refreshTtl = Duration.ofDays(30)
        val memberId = MemberId.generate()
        val deviceId = DeviceId("device-1")

        Given("활성 RefreshToken 으로 로그아웃 요청") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val useCase = LogoutUseCase(refreshTokenOutPort, clock)

            val issued = RefreshToken.issue(memberId, deviceId, "label", refreshTtl, now.minusSeconds(3600))
            val command = LogoutCommand(refreshToken = issued.plainValue)

            every { refreshTokenOutPort.findByHash(issued.entity.tokenHash) } returns issued.entity
            val saved = slot<RefreshToken>()
            every { refreshTokenOutPort.save(capture(saved)) } answers { firstArg() }

            When("execute 를 호출하면") {
                useCase.execute(command)

                Then("토큰이 revoke 되어 저장된다") {
                    verify(exactly = 1) { refreshTokenOutPort.save(any()) }
                    saved.captured.id shouldBe issued.entity.id
                    saved.captured.isRevoked() shouldBe true
                    saved.captured.revokedAt shouldBe now
                }
            }
        }

        Given("DB 에 존재하지 않는 RefreshToken 으로 로그아웃 요청") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val useCase = LogoutUseCase(refreshTokenOutPort, clock)

            val command = LogoutCommand(refreshToken = "unknown-token")
            every {
                refreshTokenOutPort.findByHash(RefreshToken.hashOfPlain("unknown-token"))
            } returns null

            When("execute 를 호출하면") {
                useCase.execute(command)

                Then("멱등 — 예외 없이 정상 종료하고 save 는 호출되지 않는다") {
                    verify(exactly = 0) { refreshTokenOutPort.save(any()) }
                }
            }
        }

        Given("이미 revoke 된 RefreshToken 으로 로그아웃 요청") {
            val refreshTokenOutPort = mockk<RefreshTokenOutPort>()
            val useCase = LogoutUseCase(refreshTokenOutPort, clock)

            val issued = RefreshToken.issue(memberId, deviceId, null, refreshTtl, now.minusSeconds(3600))
            val revoked = issued.entity.revoke(now.minusSeconds(60))
            val command = LogoutCommand(refreshToken = issued.plainValue)

            every { refreshTokenOutPort.findByHash(issued.entity.tokenHash) } returns revoked

            When("execute 를 호출하면") {
                useCase.execute(command)

                Then("멱등 — save 가 호출되지 않는다") {
                    verify(exactly = 0) { refreshTokenOutPort.save(any()) }
                }
            }
        }
    })
