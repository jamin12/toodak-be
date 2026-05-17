package org.toodakbe.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

/**
 * 시간 제공자 빈.
 *
 * UseCase 및 어댑터에서 `Clock`을 주입받아 `clock.instant()`를 호출한다.
 * 테스트에서는 `Clock.fixed(...)`를 주입해 결정론적인 시간 제어가 가능하다.
 */
@Configuration
class ClockConfig {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
