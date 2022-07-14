package ru.spb.ivsamokhvalov.example.demo.camunda.controller

import java.math.BigDecimal
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.randomizers.misc.EnumRandomizer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.OrderStatus
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CreateItemsRequest
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CreateOrderRequest
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CreatePostingRequest
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CurrencyCode
import ru.spb.ivsamokhvalov.example.demo.camunda.service.DomainService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.OrderWithPosting

@RestController
@RequestMapping("/order")
class OrderController(
    private val domainService: DomainService,

    ) {

    private val easyRandom = EasyRandom(EasyRandomParameters().also {
        it.seed = System.currentTimeMillis()
        it.randomize(CurrencyCode::class.java, EnumRandomizer(CurrencyCode::class.java, CurrencyCode.UNDEFINED))
    })

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long): OrderWithPosting {
        return domainService.getOrder(orderId)
    }


    @PostMapping("/create")
    fun createOrder(@RequestBody request: CreateOrderRequest): OrderWithPosting {
        return domainService.createOrder(request)
    }

    @PostMapping("/createRandom")
    fun createRandom(@RequestBody request: CreateRandomPostingRequest?): OrderWithPosting {
        val postingCount = request?.postingCount ?: 3
        val skuCount = request?.skuCount ?: 3

        require(postingCount > 0 && skuCount > 0) {
            "postingCount and postingCount must me grater that 0"
        }
        return domainService.createOrder(CreateOrderRequest(
            postings = IntRange(1, postingCount).map {
                CreatePostingRequest(
                    currencyCode = easyRandom.nextObject(CurrencyCode::class.java),
                    items = IntRange(1, skuCount).map {
                        CreateItemsRequest(
                            skuId = easyRandom.nextInt(100),
                            price = BigDecimal(100 + easyRandom.nextInt(1000)),
                            qty = easyRandom.nextInt(5)
                        )
                    }
                )
            }
        ))
    }

    @PostMapping("/updateStatus")
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderWithPosting {
        domainService.updateOrderStatus(orderId, newStatus)
        return domainService.getOrder(orderId)
    }
}


data class CreateRandomPostingRequest(
    var postingCount: Int? = null,
    var skuCount: Int? = null,
)