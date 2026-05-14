plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// gradle/libs.versions.toml 의 [versions] 와 반드시 동기화한다. (version-sync)
object Versions {
    const val KOTLIN = "2.2.21"
    const val SPRING_BOOT = "4.0.6"
    const val SPRING_DEPENDENCY_MANAGEMENT = "1.1.7"
    const val KTLINT = "14.2.0"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:${Versions.KOTLIN}")
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin:${Versions.KOTLIN}")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:${Versions.SPRING_BOOT}")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:${Versions.SPRING_DEPENDENCY_MANAGEMENT}")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:${Versions.KTLINT}")
}
