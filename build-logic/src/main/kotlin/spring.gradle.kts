import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.findByType
import org.springframework.boot.gradle.tasks.bundling.BootJar

// common + Spring Boot + 기본 의존성. 라이브러리 모듈 기본값(bootJar 비활성화, jar 활성화).
plugins {
    id("common")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

val catalog = extensions.findByType<VersionCatalogsExtension>()?.named("libs")

dependencies {
    catalog?.let {
        add("implementation", it.findLibrary("spring-boot-starter").get())
        add("testImplementation", it.findLibrary("spring-boot-starter-test").get())
    }
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
