package org.toodakbe.application.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.toodakbe.application.auth.dto.AccessTokenClaims
import org.toodakbe.application.auth.exception.InvalidAccessTokenException
import org.toodakbe.application.auth.port.outbound.JwtOutPort
import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

class VerifyAccessTokenUseCaseTest :
    BehaviorSpec({
        val jwtOutPort = mockk<JwtOutPort>()
        val useCase = VerifyAccessTokenUseCase(jwtOutPort)

        Given("유효한 Access Token이 주어졌을 때") {
            val token = "valid.jwt.token"
            val expectedClaims =
                AccessTokenClaims(
                    memberId = MemberId.generate(),
                    expiresAt = Instant.parse("2026-12-31T23:59:59Z"),
                )
            every { jwtOutPort.parseAccessToken(token) } returns expectedClaims

            When("verify를 호출하면") {
                val result = useCase.verify(token)

                Then("JwtOutPort가 반환한 클레임을 그대로 돌려준다") {
                    result shouldBe expectedClaims
                    verify(exactly = 1) { jwtOutPort.parseAccessToken(token) }
                }
            }
        }

        Given("JwtOutPort가 검증 실패를 던지는 토큰") {
            val token = "invalid.jwt.token"
            every { jwtOutPort.parseAccessToken(token) } throws InvalidAccessTokenException()

            When("verify를 호출하면") {
                Then("InvalidAccessTokenException이 그대로 전파된다") {
                    shouldThrow<InvalidAccessTokenException> {
                        useCase.verify(token)
                    }
                    verify(exactly = 1) { jwtOutPort.parseAccessToken(token) }
                }
            }
        }
    })
