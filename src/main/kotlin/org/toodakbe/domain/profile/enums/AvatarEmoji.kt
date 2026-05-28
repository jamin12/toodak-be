package org.toodakbe.domain.profile.enums

/**
 * 아바타 이모지 고정 풀. 첫 값이 가입 시 기본값.
 *
 * 실제 글리프(🍓 등) 매핑은 프론트 디자인 토큰이 소유한다 — 백엔드는 의미 식별자만 저장·전달한다.
 */
enum class AvatarEmoji {
    STRAWBERRY,
    PEACH,
    GRAPE,
    LEMON,
    AVOCADO,
    CHERRY,
}
