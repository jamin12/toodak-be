package org.toodakbe.adapter.auth.googleOut

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Google OAuth 설정. `application.yaml` 의 `google.*` 키와 바인딩.
 *
 * @property audience 발급받은 Google OAuth Client ID. ID Token 의 `aud` claim 검증에 사용.
 */
@ConfigurationProperties(prefix = "google")
data class GoogleOAuthProperties(
    val audience: String,
)
