package org.toodakbe.domain.profile.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.toodakbe.domain.member.vo.MemberId
import org.toodakbe.domain.profile.enums.AvatarEmoji
import org.toodakbe.domain.profile.enums.AvatarTone
import org.toodakbe.domain.profile.vo.Nickname
import java.time.Instant

class ProfileTest :
    BehaviorSpec({
        val memberId = MemberId.generate()
        val now = Instant.parse("2026-05-27T10:00:00Z")

        Given("Profile.create") {
            When("가입 직후 기본 프로필을 만들면") {
                val profile = Profile.create(memberId, Nickname("투닥러"), now)

                Then("이모지·톤은 풀의 첫 값이다") {
                    profile.emoji shouldBe AvatarEmoji.entries.first()
                    profile.tone shouldBe AvatarTone.entries.first()
                }
                Then("닉네임이 보존된다") {
                    profile.nickname shouldBe Nickname("투닥러")
                }
                Then("createdAt/updatedAt이 now로 설정된다") {
                    profile.createdAt shouldBe now
                    profile.updatedAt shouldBe now
                }
            }
        }

        Given("Profile.update") {
            val profile = Profile.create(memberId, Nickname("투닥러"), now)

            When("닉·이모지·톤을 갱신하면") {
                val later = now.plusSeconds(60)
                val updated = profile.update(Nickname("지윤"), AvatarEmoji.PEACH, AvatarTone.BLUE, later)

                Then("새 값이 반영된다") {
                    updated.nickname shouldBe Nickname("지윤")
                    updated.emoji shouldBe AvatarEmoji.PEACH
                    updated.tone shouldBe AvatarTone.BLUE
                }
                Then("memberId·createdAt은 유지되고 updatedAt만 갱신된다") {
                    updated.memberId shouldBe memberId
                    updated.createdAt shouldBe now
                    updated.updatedAt shouldBe later
                }
            }
        }
    })
