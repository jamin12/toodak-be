package org.toodakbe.domain.profile.vo

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class NicknameTest :
    BehaviorSpec({
        Given("Nickname 생성") {
            When("정상 값이면") {
                Then("값이 보존된다") {
                    Nickname("지윤").value shouldBe "지윤"
                }
                Then("최대 길이(8자)까지 허용된다") {
                    Nickname("12345678").value shouldBe "12345678"
                }
            }

            When("공백이면") {
                Then("예외가 발생한다") {
                    shouldThrow<IllegalArgumentException> { Nickname(" ") }
                }
            }

            When("8자를 초과하면") {
                Then("예외가 발생한다") {
                    shouldThrow<IllegalArgumentException> { Nickname("123456789") }
                }
            }
        }
    })
