package org.toodakbe.domain.couple.vo

import java.security.SecureRandom

/**
 * 초대 코드 문자열 = 공유 링크 토큰.
 *
 * 사람이 입력·공유하는 짧은 코드라 충돌이 가능하다 — VO는 후보를 [generate]하고,
 * 시스템 전체 유일성은 DB unique 제약 + 충돌 시 재생성(앱 레이어)으로 보장한다.
 */
@JvmInline
value class InviteCode(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "초대 코드는 비어 있을 수 없습니다" }
    }

    companion object {
        // 혼동 문자(0/O, 1/I/L) 제외 — 입력·구두 전달 오류 방지.
        private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        private const val LENGTH = 8
        private val RANDOM = SecureRandom()

        fun generate(): InviteCode {
            val chars = CharArray(LENGTH) { ALPHABET[RANDOM.nextInt(ALPHABET.length)] }
            return InviteCode(String(chars))
        }
    }
}
