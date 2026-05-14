import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.findByType
import org.springframework.boot.gradle.tasks.bundling.BootJar

// spring + 실행 가능 모듈 설정(bootJar 활성화 + web). 실행 모듈은 프로젝트에 하나만 존재해야 한다.
plugins {
    id("spring")
}

val catalog = extensions.findByType<VersionCatalogsExtension>()?.named("libs")

dependencies {
    catalog?.let {
        add("implementation", it.findLibrary("spring-boot-starter-web").get())
    }
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}
