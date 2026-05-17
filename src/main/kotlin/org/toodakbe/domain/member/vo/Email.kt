package org.toodakbe.domain.member.vo

/**
 * 회원의 이메일.
 *
 * 식별 키가 아닌 부가 정보(연락처)로 사용한다. 시스템 전체에서 unique 제약을 두지 않는다.
 * 식별은 `(provider, providerUserId)` 조합으로 수행한다.
 */
@JvmInline
value class Email(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "이메일은 비어 있을 수 없습니다" }
        require(PATTERN.matches(value)) { "이메일 형식이 올바르지 않습니다: $value" }
    }

    companion object {
        private val PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
