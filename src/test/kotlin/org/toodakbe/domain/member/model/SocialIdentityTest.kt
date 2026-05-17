package org.toodakbe.domain.member.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.toodakbe.domain.member.vo.MemberId
import org.toodakbe.domain.member.vo.Provider
import org.toodakbe.domain.member.vo.ProviderUserId
import java.time.Instant

class SocialIdentityTest :
    BehaviorSpec({
        val memberId = MemberId.generate()
        val now = Instant.parse("2026-05-15T10:00:00Z")

        Given("SocialIdentity.link") {
            When("Google 사용자를 연결하면 (이메일 검증됨)") {
                val identity =
                    SocialIdentity.link(
                        memberId = memberId,
                        provider = Provider.GOOGLE,
                        providerUserId = ProviderUserId("108249xxx"),
                        emailVerifiedAt = now,
                        now = now,
                    )

                Then("id가 자동 생성된다 (UUID v7)") {
                    identity.id.value.version() shouldBe 7
                }
                Then("memberId가 보존된다") {
                    identity.memberId shouldBe memberId
                }
                Then("provider/providerUserId가 설정된다") {
                    identity.provider shouldBe Provider.GOOGLE
                    identity.providerUserId shouldBe ProviderUserId("108249xxx")
                }
                Then("linkedAt이 주입된 now와 같다") {
                    identity.linkedAt shouldBe now
                }
                Then("emailVerifiedAt이 보존된다") {
                    identity.emailVerifiedAt.shouldNotBeNull()
                    identity.emailVerifiedAt shouldBe now
                }
            }

            When("미검증 이메일로 연결하면 (Kakao 등)") {
                val identity =
                    SocialIdentity.link(
                        memberId = memberId,
                        provider = Provider.KAKAO,
                        providerUserId = ProviderUserId("12345"),
                        emailVerifiedAt = null,
                        now = now,
                    )

                Then("emailVerifiedAt이 null로 유지된다") {
                    identity.emailVerifiedAt shouldBe null
                }
            }
        }
    })
