package org.toodakbe.domain.member.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.toodakbe.domain.member.enums.MemberStatus
import org.toodakbe.domain.member.exception.MemberNotActiveException
import org.toodakbe.domain.member.vo.Email
import java.time.Instant

class MemberTest :
    BehaviorSpec({
        val now = Instant.parse("2026-05-15T10:00:00Z")

        Given("Member.register") {
            When("유효한 이메일로 가입하면") {
                val member = Member.register(Email("user@gmail.com"), now)

                Then("status는 ACTIVE이다") {
                    member.status shouldBe MemberStatus.ACTIVE
                }
                Then("withdrawnAt은 null이다") {
                    member.withdrawnAt shouldBe null
                }
                Then("createdAt이 주입된 now와 같다") {
                    member.createdAt shouldBe now
                }
                Then("id가 UUID v7로 자동 생성된다") {
                    member.id.value.version() shouldBe 7
                }
            }
        }

        Given("ACTIVE 회원의 changeEmail") {
            val member = Member.register(Email("old@gmail.com"), now)

            When("다른 이메일로 변경하면") {
                val updated = member.changeEmail(Email("new@gmail.com"))

                Then("새 인스턴스를 반환한다 (immutable)") {
                    updated shouldNotBe member
                }
                Then("새 인스턴스의 email이 변경되어 있다") {
                    updated.email shouldBe Email("new@gmail.com")
                }
                Then("id, createdAt은 유지된다") {
                    updated.id shouldBe member.id
                    updated.createdAt shouldBe member.createdAt
                }
            }

            When("같은 이메일로 변경하면") {
                val updated = member.changeEmail(Email("old@gmail.com"))

                Then("동일 인스턴스를 반환한다 (불필요한 객체 생성 회피)") {
                    updated shouldBe member
                }
            }
        }

        Given("WITHDRAWN 회원의 changeEmail") {
            val withdrawn = Member.register(Email("user@gmail.com"), now).withdraw(now)

            When("이메일 변경을 시도하면") {
                Then("MemberNotActiveException이 발생한다") {
                    shouldThrow<MemberNotActiveException> {
                        withdrawn.changeEmail(Email("new@gmail.com"))
                    }
                }
            }
        }

        Given("ACTIVE 회원의 withdraw") {
            val member = Member.register(Email("user@gmail.com"), now)
            val withdrawnAt = now.plusSeconds(60)

            When("탈퇴 처리하면") {
                val withdrawn = member.withdraw(withdrawnAt)

                Then("status가 WITHDRAWN이 된다") {
                    withdrawn.status shouldBe MemberStatus.WITHDRAWN
                }
                Then("withdrawnAt이 주입된 시각으로 설정된다") {
                    withdrawn.withdrawnAt.shouldNotBeNull()
                    withdrawn.withdrawnAt shouldBe withdrawnAt
                }
            }
        }

        Given("이미 WITHDRAWN인 회원의 withdraw") {
            val withdrawn = Member.register(Email("user@gmail.com"), now).withdraw(now)

            When("다시 탈퇴 처리하면") {
                val again = withdrawn.withdraw(now.plusSeconds(60))

                Then("동일 인스턴스를 반환한다 (멱등)") {
                    again shouldBe withdrawn
                }
            }
        }
    })
