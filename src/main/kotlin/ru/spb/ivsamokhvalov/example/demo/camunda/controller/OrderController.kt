package ru.spb.ivsamokhvalov.example.demo.camunda.controller

import java.math.BigDecimal
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CreateItemsRequest
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CreateOrderRequest
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CreatePostingRequest
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CurrencyCode
import ru.spb.ivsamokhvalov.example.demo.camunda.service.MainService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.OrderStatus
import ru.spb.ivsamokhvalov.example.demo.camunda.service.OrderWithPosting
import ru.spb.ivsamokhvalov.example.demo.camunda.service.UpdateOrderRequest

@RestController
@RequestMapping("/order")
class OrderController(
    private val mainService: MainService,
) {

    private val easyRandom = EasyRandom(EasyRandomParameters().also {
        it.seed = System.currentTimeMillis()
    })

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long): OrderWithPosting {
        return mainService.getOrder(orderId)
    }

    @PostMapping("/create")
    fun createOrder(@RequestBody request: CreateOrderRequest): OrderWithPosting {
        return mainService.createOrder(request)
    }

    @PostMapping("/createRandom")
    fun createRandom(): OrderWithPosting {
        return mainService.createOrder(CreateOrderRequest(
            postings = IntRange(1, 3).map {
                CreatePostingRequest(
                    currencyCode = easyRandom.nextObject(CurrencyCode::class.java),
                    items = IntRange(1, 3).map {
                        CreateItemsRequest(
                            skuId = easyRandom.nextInt(100),
                            price = easyRandom.nextObject(BigDecimal::class.java),
                            qty = easyRandom.nextInt(5)
                        )
                    }
                )
            }
        ))
    }

    @PostMapping("/updateStatus")
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderWithPosting {
        mainService.updateOrder(UpdateOrderRequest(orderId = orderId, orderStatus = newStatus))
        return mainService.getOrder(orderId)
    }
}
