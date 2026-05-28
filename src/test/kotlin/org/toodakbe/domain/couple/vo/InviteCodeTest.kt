package org.toodakbe.domain.couple.vo

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class InviteCodeTest :
    BehaviorSpec({
        Given("InviteCode 생성") {
            When("정상 값이면") {
                Then("값이 보존된다") {
                    InviteCode("T7K9QX2M").value shouldBe "T7K9QX2M"
                }
            }

            When("공백이면") {
                Then("예외가 발생한다") {
                    shouldThrow<IllegalArgumentException> { InviteCode(" ") }
                }
            }
        }

        Given("InviteCode.generate") {
            When("코드를 생성하면") {
                val code = InviteCode.generate()

                Then("8자이고 혼동 문자(0/O, 1/I/L)를 포함하지 않는다") {
                    code.value.length shouldBe 8
                    code.value.all { it in "ABCDEFGHJKMNPQRSTUVWXYZ23456789" } shouldBe true
                }
            }

            When("여러 번 생성하면") {
                Then("매번 다른 값이 나온다") {
                    InviteCode.generate() shouldNotBe InviteCode.generate()
                }
            }
        }
    })
