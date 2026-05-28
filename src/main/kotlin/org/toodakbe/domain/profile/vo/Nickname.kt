package org.toodakbe.domain.profile.vo

/**
 * 표시용 닉네임. 카드 헤더·sideTag·커플 아바타에 들어가므로 길이를 짧게 제한한다.
 */
@JvmInline
value class Nickname(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "닉네임은 비어 있을 수 없습니다" }
        require(value.length <= MAX_LENGTH) { "닉네임은 ${MAX_LENGTH}자 이하여야 합니다: $value" }
    }

    companion object {
        const val MAX_LENGTH = 8
    }
}
