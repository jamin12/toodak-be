package org.toodakbe.domain.couple.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.toodakbe.domain.couple.enums.InviteStatus
import org.toodakbe.domain.couple.vo.InviteCode
import org.toodakbe.domain.member.vo.MemberId
import java.time.Duration
import java.time.Instant

class PairingInviteTest :
    BehaviorSpec({
        val inviter = MemberId.generate()
        val joiner = MemberId.generate()
        val now = Instant.parse("2026-05-27T10:00:00Z")
        val expiresAt = now.plus(Duration.ofHours(24))

        fun issue() = PairingInvite.issue(InviteCode("T7K-9QX2"), inviter, expiresAt, now)

        Given("PairingInvite.issue") {
            When("발급하면") {
                val invite = issue()

                Then("id가 UUID v7로 발급된다") {
                    invite.id.value.version() shouldBe 7
                }
                Then("PENDING 상태이고 수락 정보는 비어 있다") {
                    invite.status shouldBe InviteStatus.PENDING
                    invite.acceptedBy.shouldBeNull()
                    invite.acceptedAt.shouldBeNull()
                }
                Then("만료 전이면 usable이다") {
                    invite.usable(now) shouldBe true
                }
            }
        }

        Given("PairingInvite.usable") {
            val invite = issue()

            When("만료 시각 이후면") {
                Then("usable하지 않다") {
                    invite.usable(expiresAt) shouldBe false
                    invite.usable(expiresAt.plusSeconds(1)) shouldBe false
                }
            }
        }

        Given("PairingInvite.accept") {
            When("합류자가 유효한 초대를 수락하면") {
                val accepted = issue().accept(joiner, now.plusSeconds(10))

                Then("ACCEPTED로 전이되고 수락 정보가 채워진다") {
                    accepted.status shouldBe InviteStatus.ACCEPTED
                    accepted.acceptedBy shouldBe joiner
                    accepted.acceptedAt shouldBe now.plusSeconds(10)
                }
                Then("다시 수락할 수 없다(1회용)") {
                    shouldThrow<IllegalArgumentException> { accepted.accept(joiner, now.plusSeconds(20)) }
                }
            }

            When("초대자가 자신의 코드를 수락하면") {
                Then("예외가 발생한다") {
                    shouldThrow<IllegalArgumentException> { issue().accept(inviter, now) }
                }
            }

            When("만료된 초대를 수락하면") {
                Then("예외가 발생한다") {
                    shouldThrow<IllegalArgumentException> { issue().accept(joiner, expiresAt) }
                }
            }
        }

        Given("PairingInvite.revoke") {
            When("PENDING 초대를 무효화하면") {
                val revoked = issue().revoke()

                Then("REVOKED로 전이되고 usable하지 않다") {
                    revoked.status shouldBe InviteStatus.REVOKED
                    revoked.usable(now) shouldBe false
                }
            }

            When("이미 수락된 초대를 무효화하면") {
                val accepted = issue().accept(joiner, now.plusSeconds(10))

                Then("상태가 유지된다(멱등)") {
                    accepted.revoke().status shouldBe InviteStatus.ACCEPTED
                }
            }
        }
    })
