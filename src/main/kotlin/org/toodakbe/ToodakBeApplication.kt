package org.toodakbe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ToodakBeApplication

fun main(args: Array<String>) {
    runApplication<ToodakBeApplication>(*args)
}
