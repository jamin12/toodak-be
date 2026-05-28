package org.toodakbe.domain.profile.model

import org.toodakbe.domain.member.vo.MemberId
import org.toodakbe.domain.profile.enums.AvatarEmoji
import org.toodakbe.domain.profile.enums.AvatarTone
import org.toodakbe.domain.profile.vo.Nickname
import java.time.Instant

/**
 * 회원의 표시 정체성(닉네임·이모지·톤색). Member와 1:1.
 *
 * 도메인 불변식:
 * - [memberId]가 곧 식별자 — 회원당 프로필은 정확히 하나(별도 surrogate id 없음).
 * - 가입 직후 기본값으로 생성되고, 설정 화면에서 세 값을 한 번에 갱신한다.
 */
class Profile private constructor(
    val memberId: MemberId,
    val nickname: Nickname,
    val emoji: AvatarEmoji,
    val tone: AvatarTone,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun update(
        nickname: Nickname,
        emoji: AvatarEmoji,
        tone: AvatarTone,
        now: Instant,
    ): Profile =
        Profile(
            memberId = memberId,
            nickname = nickname,
            emoji = emoji,
            tone = tone,
            createdAt = createdAt,
            updatedAt = now,
        )

    companion object {
        /**
         * 가입 직후 기본 프로필. 닉네임은 앱에서 생성해 주입하고, 이모지·톤은 풀의 첫 값을 쓴다.
         */
        fun create(
            memberId: MemberId,
            nickname: Nickname,
            now: Instant,
        ): Profile =
            Profile(
                memberId = memberId,
                nickname = nickname,
                emoji = AvatarEmoji.entries.first(),
                tone = AvatarTone.entries.first(),
                createdAt = now,
                updatedAt = now,
            )

        /**
         * 영속화 어댑터에서 도메인 모델을 복원할 때 사용.
         */
        fun restore(
            memberId: MemberId,
            nickname: Nickname,
            emoji: AvatarEmoji,
            tone: AvatarTone,
            createdAt: Instant,
            updatedAt: Instant,
        ): Profile =
            Profile(
                memberId = memberId,
                nickname = nickname,
                emoji = emoji,
                tone = tone,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
    }
}
