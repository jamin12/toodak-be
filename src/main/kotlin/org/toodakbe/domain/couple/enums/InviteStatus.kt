package org.toodakbe.domain.couple.enums

/**
 * 페어링 초대 상태.
 *
 * - [PENDING]: 발급됨, 수락 대기.
 * - [ACCEPTED]: 수락되어 커플 연결 완료(1회용 소진).
 * - [EXPIRED]: 유효기간 경과.
 * - [REVOKED]: 재발급·취소로 무효화.
 */
enum class InviteStatus {
    PENDING,
    ACCEPTED,
    EXPIRED,
    REVOKED,
}
