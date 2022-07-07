package ru.spb.ivsamokhvalov.example.demo.camunda

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoCamundaApplication

fun main(args: Array<String>) {
	runApplication<DemoCamundaApplication>(*args)
}
