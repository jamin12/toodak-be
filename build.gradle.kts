// 실행 가능 모듈: app convention plugin 적용 (spring + bootJar + web)
// jpa convention plugin 추가: kotlin-plugin-jpa + all-open + spring-boot-starter-data-jpa
// kotlin-kapt: MapStruct annotation processor 용
// flyway: 수동 마이그레이션 실행용 (./gradlew flywayMigrate)
//   Flyway 11.x Gradle plugin은 JDBC driver를 buildscript classpath에서 찾으므로
//   PostgreSQL driver를 buildscript {} 블록에 추가한다.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.postgresql:postgresql:42.7.4")
    }
}

plugins {
    id("app")
    id("jpa")
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.flyway)
}

group = "org"
version = "0.0.1-SNAPSHOT"
description = "toodak-be"

dependencies {
    // 인증/인가 + 입력 검증
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)

    // DB (PostgreSQL + Flyway). 버전은 Spring Boot BOM 위임.
    runtimeOnly(libs.postgresql)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgresql)

    // JWT
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // 매퍼 (MapStruct). REST 매퍼만 사용. annotation processor는 kapt.
    implementation(libs.mapstruct)
    kapt(libs.mapstruct.processor)

    // Google ID Token 검증 (GoogleIdTokenVerifier)
    implementation(libs.google.api.client)

    // UUID v7 (Java 표준엔 v4까지만)
    implementation(libs.uuid.creator)

    // 테스트
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.instancio.core)
    testImplementation(libs.instancio.junit)
}

// 수동 마이그레이션 설정. 환경변수로 override 가능 (DB_URL/DB_USERNAME/DB_PASSWORD).
// 자동 호출은 application.yaml의 `spring.flyway.enabled: false`로 차단.
// 사용:
//   ./gradlew flywayInfo      # 마이그레이션 상태 확인
//   ./gradlew flywayMigrate   # 미적용 마이그레이션 실행
//   ./gradlew flywayValidate  # 적용된 마이그레이션 검증
//   ./gradlew flywayClean     # (개발 전용) 스키마 초기화 — clean-disabled=false 필요
flyway {
    url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/toodak"
    user = System.getenv("DB_USERNAME") ?: "toodak"
    password = System.getenv("DB_PASSWORD") ?: "toodak"
    schemas = arrayOf("public")
    locations = arrayOf("filesystem:src/main/resources/db/migration")
    cleanDisabled = (System.getenv("FLYWAY_CLEAN_DISABLED") ?: "true").toBoolean()
}
