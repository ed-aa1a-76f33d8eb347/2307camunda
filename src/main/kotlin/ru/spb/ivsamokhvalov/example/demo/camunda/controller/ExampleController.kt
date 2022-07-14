package ru.spb.ivsamokhvalov.example.demo.camunda.controller

import java.math.BigDecimal
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.RuntimeService
import org.springframework.data.repository.CrudRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CreateOrderRequest
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CurrenciesConverterService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CurrencyCode
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CurrencyPrice
import ru.spb.ivsamokhvalov.example.demo.camunda.service.DomainService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.Order
import ru.spb.ivsamokhvalov.example.demo.camunda.service.OrderService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.OrderStatus
import ru.spb.ivsamokhvalov.example.demo.camunda.service.Posting
import ru.spb.ivsamokhvalov.example.demo.camunda.service.PostingService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.PostingStatus
import ru.spb.ivsamokhvalov.example.demo.camunda.service.UpdatePostingRequest

//@RestController
class ExampleController(
//    private val orderRepository: OrderRepository,
//    private val postingRepository: PostingRepository,
    private val currenciesConverter: CurrenciesConverterService,
    private val domainService: DomainService,
    private val orderService: OrderService,
    private val postingService: PostingService,
    private val runtimeService: RuntimeService,
    private val processEngine: ProcessEngine,

    ) {

    @GetMapping("/test")
    fun getValue(): String {
        val process = runtimeService.startProcessInstanceByKey(
            "Process_03gfg8y",
            UUID.randomUUID().toString()
        )
        return UUID.randomUUID().toString()
    }

    @GetMapping("/create")
    fun create(): Order {
        return orderService.createOrder(CreateOrderRequest(emptyList()))
    }

    @GetMapping("/getOrder/{id}")
    fun getOrder(@PathVariable("id") id: Long) = domainService.getOrder(id)

    @PostMapping("/convert")
    fun convertCurrencies(@RequestBody request: ConvertCurrenciesRequest): CurrencyPrice {
        return currenciesConverter.convert(CurrencyPrice(request.price, request.fromCode), request.toCode)
    }

    @PostMapping("/posting/{postingId}/updateStatus")
    fun updatePostingStatus(@PathVariable postingId: Long, @RequestParam status: PostingStatus): Posting {
        postingService.updatePosting(UpdatePostingRequest(postingId = postingId, postingStatus = status))
        return postingService.getPosting(postingId)
    }

    @PostMapping("/order/{orderId}/recalculate")
    fun recalculateOrderStatus(@PathVariable orderId: Long): Order {
        domainService.recalculateOrderStatus(orderId)
        return orderService.getOrder(orderId)
    }

}

data class ConvertCurrenciesRequest(
    val price: BigDecimal,
    val fromCode: CurrencyCode,
    val toCode: CurrencyCode,
)


@Entity(name = "orders")
data class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    override var orderId: Long = 0,
    override var orderStatus: OrderStatus = OrderStatus.CREATED,
    var currency: CurrencyCode = CurrencyCode.UNDEFINED,
    var price: BigDecimal = BigDecimal.ZERO,

    ) : Order

@Entity(name = "postings")
data class PostingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "postings_seq")
    var postingId: Long = 0,
    val orderId: Long,
    var postingStatus: PostingStatus = PostingStatus.AWAITING_PAYMENT,
    var currency: CurrencyCode = CurrencyCode.UNDEFINED,
    var price: BigDecimal = BigDecimal.ZERO,
)

@Entity(name = "items")
data class ItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "items_seq")
    var itemId: Long = 0,
    val postingId: Long,
    val skuId: Int,
    val qty: Int,
    val price: BigDecimal,
)


interface OrderRepository : CrudRepository<OrderEntity, Long>
interface PostingRepository : CrudRepository<PostingEntity, Long> {
    fun findByOrderIdOrderByPostingIdAsc(orderId: Long): List<PostingEntity>
}

interface ItemRepository : CrudRepository<ItemEntity, Long> {
    fun findByPostingIdOrderByItemIdAsc(postingId: Long): List<ItemEntity>
}