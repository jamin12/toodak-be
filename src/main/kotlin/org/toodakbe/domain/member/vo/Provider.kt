package org.toodakbe.domain.member.vo

/**
 * 소셜 로그인 제공자.
 *
 * @property trustsEmailVerification 해당 제공자가 사용자의 이메일 소유를 검증해주는지 여부.
 *   자동 계정 연결(같은 이메일로 다른 제공자가 들어왔을 때 기존 Member에 연결)은
 *   이 플래그가 `true`인 제공자에 대해서만 허용한다. 이메일 탈취 기반 계정 가로채기 방어.
 */
enum class Provider(
    val trustsEmailVerification: Boolean,
) {
    GOOGLE(true),
    KAKAO(false),
    APPLE(true),
}
