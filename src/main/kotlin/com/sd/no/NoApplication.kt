package com.sd.no

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class NoApplication

fun main(args: Array<String>) {
    runApplication<NoApplication>(*args)
}
