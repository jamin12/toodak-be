import org.gradle.kotlin.dsl.findByType

// JPA 전용 설정. spring 플러그인과 조합하여 필요한 모듈에만 적용한다. (plugin-single-responsibility)
plugins {
    kotlin("plugin.jpa")
}

val catalog = extensions.findByType<VersionCatalogsExtension>()?.named("libs")

dependencies {
    catalog?.let {
        add("implementation", it.findLibrary("spring-boot-starter-data-jpa").get())
    }
}
