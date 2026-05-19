package org.toodakbe.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * QueryDSL [JPAQueryFactory] 빈 등록.
 *
 * JPA 영속성 컨텍스트의 [EntityManager] 를 위임하므로 Spring 트랜잭션 경계에 자연스럽게 참여한다.
 * Bulk UPDATE/DELETE 는 1차 캐시를 우회하므로 호출 측에서 `flush/clear` 책임을 진다.
 */
@Configuration
class QueryDslConfig(
    private val entityManager: EntityManager,
) {
    @Bean
    fun jpaQueryFactory(): JPAQueryFactory = JPAQueryFactory(entityManager)
}
