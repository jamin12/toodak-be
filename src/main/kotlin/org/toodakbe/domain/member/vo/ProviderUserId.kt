package org.toodakbe.domain.member.vo

/**
 * 소셜 제공자가 발급한 사용자 식별자.
 *
 * - Google: `sub` claim
 * - Kakao: 회원번호 (id)
 * - Apple: `sub` claim
 *
 * 제공자가 보장하는 영구 불변 값. `(Provider, ProviderUserId)` 조합이 시스템 전체에서 unique.
 */
@JvmInline
value class ProviderUserId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "providerUserId는 비어 있을 수 없습니다" }
    }
}
