package org.toodakbe.domain.couple.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.toodakbe.domain.member.vo.MemberId
import java.time.Instant

class CoupleTest :
    BehaviorSpec({
        val inviter = MemberId.generate()
        val joiner = MemberId.generate()
        val now = Instant.parse("2026-05-27T10:00:00Z")

        Given("Couple.create") {
            When("초대자와 합류자로 생성하면") {
                val couple = Couple.create(inviter, joiner, now)

                Then("id가 UUID v7로 발급된다") {
                    couple.id.value.version() shouldBe 7
                }
                Then("memberA=초대자, memberB=합류자, pairedAt=now") {
                    couple.memberAId shouldBe inviter
                    couple.memberBId shouldBe joiner
                    couple.pairedAt shouldBe now
                }
            }

            When("같은 회원끼리 연결하면") {
                Then("예외가 발생한다") {
                    shouldThrow<IllegalArgumentException> { Couple.create(inviter, inviter, now) }
                }
            }
        }

        Given("Couple.contains / partnerOf") {
            val couple = Couple.create(inviter, joiner, now)

            When("커플에 속한 회원으로 조회하면") {
                Then("contains는 true다") {
                    couple.contains(inviter) shouldBe true
                    couple.contains(joiner) shouldBe true
                }
                Then("상대를 반환한다") {
                    couple.partnerOf(inviter) shouldBe joiner
                    couple.partnerOf(joiner) shouldBe inviter
                }
            }

            When("커플에 속하지 않은 회원으로 partnerOf를 호출하면") {
                Then("예외가 발생한다") {
                    shouldThrow<IllegalArgumentException> { couple.partnerOf(MemberId.generate()) }
                }
            }
        }
    })
