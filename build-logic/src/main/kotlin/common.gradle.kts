import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

// 모든 Kotlin 모듈 공통 설정: kotlin(jvm) + ktlint + 공통 라이브러리 + 테스트 프레임워크
plugins {
    kotlin("jvm")
    id("ktlint")
}

repositories {
    mavenCentral()
}

val catalog = extensions.findByType<VersionCatalogsExtension>()?.named("libs")

extensions.configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

extensions.configure<KotlinJvmProjectExtension> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

dependencies {
    catalog?.let {
        add("implementation", it.findLibrary("kotlin-reflect").get())
        add("implementation", it.findLibrary("jackson-module-kotlin").get())
        add("implementation", it.findLibrary("kotlin-logging").get())

        add("testImplementation", it.findLibrary("kotest-runner-junit5").get())
        add("testImplementation", it.findLibrary("kotest-assertions-core").get())
        add("testImplementation", it.findLibrary("mockk").get())
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
