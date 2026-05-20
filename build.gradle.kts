// flyway: Gradle plugin은 런타임이 아닌 buildscript classpath에서 driver/DB 핸들러를 찾는다.
// flyway-database-postgresql 은 Flyway 10+ 에서 분리된 PostgreSQL 핸들러.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.postgresql:postgresql:42.7.4")
        classpath("org.flywaydb:flyway-database-postgresql:${libs.versions.flywayPlugin.get()}")
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

    // QueryDSL (JPA bulk update / 동적 쿼리). :jakarta 분류자가 Jakarta EE 네임스페이스 지원.
    // 정적 메타모델(Q클래스)은 kapt 가 com.querydsl.apt.jpa.JPAAnnotationProcessor 로 자동 생성.
    implementation(variantOf(libs.querydsl.jpa) { classifier("jakarta") })
    kapt(variantOf(libs.querydsl.apt) { classifier("jakarta") })
    kapt(libs.jakarta.annotation.api)
    kapt(libs.jakarta.persistence.api)

    // Google ID Token 검증 (GoogleIdTokenVerifier)
    implementation(libs.google.api.client)

    // UUID v7 (Java 표준엔 v4까지만)
    implementation(libs.uuid.creator)

    // OpenAPI / Swagger UI
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

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
    url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:15432/toodak"
    user = System.getenv("DB_USERNAME") ?: "toodak"
    password = System.getenv("DB_PASSWORD") ?: "toodak"
    schemas = arrayOf("public")
    locations = arrayOf("filesystem:src/main/resources/db/migration")
    cleanDisabled = (System.getenv("FLYWAY_CLEAN_DISABLED") ?: "true").toBoolean()
}
