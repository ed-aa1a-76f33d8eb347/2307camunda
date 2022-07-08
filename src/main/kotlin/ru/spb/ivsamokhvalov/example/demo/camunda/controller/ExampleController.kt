package ru.spb.ivsamokhvalov.example.demo.camunda.controller

import java.math.BigDecimal
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import org.springframework.data.repository.CrudRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CurrencyCode
import ru.spb.ivsamokhvalov.example.demo.camunda.service.Order
import ru.spb.ivsamokhvalov.example.demo.camunda.service.OrderStatus
import ru.spb.ivsamokhvalov.example.demo.camunda.service.PostingStatus

@RestController
class ExampleController(
    private val orderRepository: OrderRepository,
) {

    @GetMapping("/test")
    fun getValue(): String {
        return UUID.randomUUID().toString()
    }

    @GetMapping("/create")
    fun create(): OrderEntity {
        val order = OrderEntity()
        return orderRepository.save(order)
    }

    @GetMapping("/getOrder/{id}")
    fun getOrder(@PathVariable("id") id: Long): OrderEntity {
        return orderRepository.findById(id).orElseThrow()
    }

}


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