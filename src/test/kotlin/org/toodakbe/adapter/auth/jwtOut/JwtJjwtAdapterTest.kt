package org.toodakbe.adapter.auth.jwtOut

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.toodakbe.application.auth.exception.InvalidAccessTokenException
import org.toodakbe.domain.member.vo.MemberId
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/**
 * [JwtJjwtAdapter] 라운드트립 테스트.
 *
 * 외부 의존(JJWT) 없이 어댑터 자체의 issue/parse 호환성, 만료 검증, 서명 검증을 단위 테스트한다.
 * Spring 컨텍스트는 띄우지 않는다.
 */
class JwtJjwtAdapterTest :
    BehaviorSpec({
        val secret = "test-secret-do-not-use-in-production-must-be-at-least-256-bits-long"
        val ttl = Duration.ofMinutes(15)
        val baseProperties =
            JwtProperties(
                secret = secret,
                accessTtl = ttl,
                refreshTtl = Duration.ofDays(30),
            )
        val now = Instant.parse("2026-05-15T10:00:00Z")
        val fixedClock = Clock.fixed(now, ZoneOffset.UTC)

        Given("정상 발급된 Access Token") {
            val adapter = JwtJjwtAdapter(baseProperties, fixedClock)
            val memberId = MemberId.generate()
            val accessToken = adapter.issueAccessToken(memberId, ttl)

            When("동일한 시크릿으로 파싱하면") {
                val claims = adapter.parseAccessToken(accessToken.value)

                Then("발급한 memberId가 복원된다") {
                    claims.memberId shouldBe memberId
                }
                Then("expiresAt이 now + ttl과 일치한다") {
                    claims.expiresAt shouldBe now.plus(ttl)
                    accessToken.expiresAt shouldBe now.plus(ttl)
                }
                Then("JWT는 header.payload.signature 형식이다") {
                    accessToken.value.split(".").size shouldBe 3
                    accessToken.value shouldStartWith "eyJ"
                }
            }
        }

        Given("만료된 Access Token") {
            val issuerAdapter = JwtJjwtAdapter(baseProperties, fixedClock)
            val accessToken = issuerAdapter.issueAccessToken(MemberId.generate(), ttl)

            When("만료 이후 시각에 파싱하면") {
                val laterClock = Clock.fixed(now.plus(ttl).plusSeconds(60), ZoneOffset.UTC)
                val laterAdapter = JwtJjwtAdapter(baseProperties, laterClock)

                Then("InvalidAccessTokenException이 발생한다") {
                    shouldThrow<InvalidAccessTokenException> {
                        laterAdapter.parseAccessToken(accessToken.value)
                    }
                }
            }
        }

        Given("다른 시크릿으로 서명된 토큰") {
            val issuerAdapter = JwtJjwtAdapter(baseProperties, fixedClock)
            val accessToken = issuerAdapter.issueAccessToken(MemberId.generate(), ttl)

            When("다른 시크릿을 가진 어댑터가 파싱하면") {
                val otherAdapter =
                    JwtJjwtAdapter(
                        baseProperties.copy(secret = "different-secret-also-at-least-256-bits-long-please-replace"),
                        fixedClock,
                    )

                Then("InvalidAccessTokenException이 발생한다") {
                    shouldThrow<InvalidAccessTokenException> {
                        otherAdapter.parseAccessToken(accessToken.value)
                    }
                }
            }
        }

        Given("문법적으로 잘못된 토큰") {
            val adapter = JwtJjwtAdapter(baseProperties, fixedClock)

            When("빈 문자열을 파싱하면") {
                Then("InvalidAccessTokenException이 발생한다") {
                    shouldThrow<InvalidAccessTokenException> {
                        adapter.parseAccessToken("")
                    }
                }
            }
            When("JWT가 아닌 문자열을 파싱하면") {
                Then("InvalidAccessTokenException이 발생한다") {
                    shouldThrow<InvalidAccessTokenException> {
                        adapter.parseAccessToken("not.a.jwt")
                    }
                }
            }
        }
    })
