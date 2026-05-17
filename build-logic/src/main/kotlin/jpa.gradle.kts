import org.gradle.kotlin.dsl.findByType

// JPA 전용 설정. spring 플러그인과 조합하여 필요한 모듈에만 적용한다. (plugin-single-responsibility)
plugins {
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")
}

// plugin.jpa 는 no-arg 생성자만 합성한다. Hibernate 지연 로딩 프록시를 위해
// JPA 엔티티 계열 클래스를 all-open 으로 열어준다. (plugin.spring 의 all-open 은 Spring 스테레오타입만 대상)
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

val catalog = extensions.findByType<VersionCatalogsExtension>()?.named("libs")

dependencies {
    catalog?.let {
        add("implementation", it.findLibrary("spring-boot-starter-data-jpa").get())
    }
}
