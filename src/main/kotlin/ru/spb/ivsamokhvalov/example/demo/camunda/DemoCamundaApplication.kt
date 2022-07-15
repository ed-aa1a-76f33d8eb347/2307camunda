package ru.spb.ivsamokhvalov.example.demo.camunda

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
@EnableProcessApplication
class DemoCamundaApplication

fun main(args: Array<String>) {
    runApplication<DemoCamundaApplication>(*args)
}
