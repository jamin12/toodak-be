package org.toodakbe.domain.auth.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.toodakbe.domain.auth.vo.DeviceId
import org.toodakbe.domain.member.vo.MemberId
import java.time.Duration
import java.time.Instant

class RefreshTokenTest :
    BehaviorSpec({
        val memberId = MemberId.generate()
        val deviceId = DeviceId("device-abc")
        val now = Instant.parse("2026-05-15T10:00:00Z")
        val ttl = Duration.ofDays(30)

        Given("RefreshToken.issue") {
            When("최초 발급하면") {
                val issued = RefreshToken.issue(memberId, deviceId, "iPhone 15", ttl, now)

                Then("id가 자동 생성된다 (UUID v7)") {
                    val tokenUuid = issued.entity.id.value
                    tokenUuid.version() shouldBe 7
                }
                Then("memberId/deviceId/deviceLabel이 보존된다") {
                    issued.entity.memberId shouldBe memberId
                    issued.entity.deviceId shouldBe deviceId
                    issued.entity.deviceLabel shouldBe "iPhone 15"
                }
                Then("expiresAt = now + ttl") {
                    issued.entity.expiresAt shouldBe now.plus(ttl)
                }
                Then("revokedAt/rotatedFromId은 null") {
                    issued.entity.revokedAt.shouldBeNull()
                    issued.entity.rotatedFromId.shouldBeNull()
                }
                Then("평문 토큰의 해시가 tokenHash와 일치한다") {
                    RefreshToken.hashOfPlain(issued.plainValue) shouldBe issued.entity.tokenHash
                }
                Then("평문 토큰은 충분히 길다 (base64url 32바이트)") {
                    issued.plainValue.length shouldBe 43 // 32바이트 base64url no-padding
                }
            }
        }

        Given("RefreshToken.revoke") {
            val issued = RefreshToken.issue(memberId, deviceId, null, ttl, now)
            val token = issued.entity

            When("revoke를 호출하면") {
                val revoked = token.revoke(now.plusSeconds(10))

                Then("revokedAt이 채워진다") {
                    revoked.revokedAt.shouldNotBeNull()
                    revoked.revokedAt shouldBe now.plusSeconds(10)
                }
                Then("isRevoked가 true다") {
                    revoked.isRevoked() shouldBe true
                }
            }

            When("이미 revoke된 토큰을 다시 revoke하면") {
                val revokedOnce = token.revoke(now.plusSeconds(10))
                val revokedTwice = revokedOnce.revoke(now.plusSeconds(20))

                Then("동일 인스턴스를 반환한다 (멱등)") {
                    revokedTwice shouldBe revokedOnce
                }
            }
        }

        Given("RefreshToken.rotate") {
            val issued = RefreshToken.issue(memberId, deviceId, "iPhone 15", ttl, now)
            val original = issued.entity

            When("회전을 호출하면") {
                val rotated = original.rotate(now.plus(Duration.ofMinutes(10)), ttl)

                Then("새 id가 발급된다") {
                    rotated.entity.id shouldNotBe original.id
                }
                Then("rotatedFromId가 원본 id를 가리킨다") {
                    rotated.entity.rotatedFromId shouldBe original.id
                }
                Then("새 평문/해시가 발급된다 (이전 것과 다름)") {
                    rotated.plainValue shouldNotBe issued.plainValue
                    rotated.entity.tokenHash shouldNotBe original.tokenHash
                }
                Then("memberId/deviceId/deviceLabel은 보존된다") {
                    rotated.entity.memberId shouldBe original.memberId
                    rotated.entity.deviceId shouldBe original.deviceId
                    rotated.entity.deviceLabel shouldBe original.deviceLabel
                }
            }
        }

        Given("RefreshToken.isExpired") {
            val issued = RefreshToken.issue(memberId, deviceId, null, ttl, now)
            val token = issued.entity

            When("만료 시각 이전이면") {
                Then("false다") {
                    token.isExpired(now.plus(Duration.ofDays(29))) shouldBe false
                }
            }

            When("만료 시각 정확히 또는 이후면") {
                Then("true다") {
                    token.isExpired(now.plus(ttl)) shouldBe true
                    token.isExpired(now.plus(ttl).plusSeconds(1)) shouldBe true
                }
            }
        }

        Given("RefreshToken.hashOfPlain") {
            When("같은 평문 두 번 해시하면") {
                Then("동일한 해시를 반환한다 (결정론적)") {
                    RefreshToken.hashOfPlain("abc") shouldBe RefreshToken.hashOfPlain("abc")
                }
            }
            When("다른 평문을 해시하면") {
                Then("다른 해시를 반환한다") {
                    RefreshToken.hashOfPlain("abc") shouldNotBe RefreshToken.hashOfPlain("abd")
                }
            }
        }
    })
