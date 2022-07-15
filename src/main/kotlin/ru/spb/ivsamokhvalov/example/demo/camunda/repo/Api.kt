package ru.spb.ivsamokhvalov.example.demo.camunda.repo

import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import org.springframework.data.repository.CrudRepository
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CurrencyCode
import ru.spb.ivsamokhvalov.example.demo.camunda.service.CurrencyPrice
import ru.spb.ivsamokhvalov.example.demo.camunda.service.Item
import ru.spb.ivsamokhvalov.example.demo.camunda.service.Order

interface OrderRepository : CrudRepository<OrderEntity, Long>
interface PostingRepository : CrudRepository<PostingEntity, Long> {
    fun findByOrderIdOrderByPostingIdAsc(orderId: Long): List<PostingEntity>
}

interface ItemRepository : CrudRepository<ItemEntity, Long> {
    fun findByPostingIdOrderByItemIdAsc(postingId: Long): List<ItemEntity>
}


@Entity(name = "orders")
data class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq")
    override var orderId: Long = 0,
    override var orderStatus: OrderStatus = OrderStatus.CREATED,
    var currency: String = CurrencyCode.UNDEFINED.name,
    var price: BigDecimal = BigDecimal.ZERO,

    ) : Order

@Entity(name = "postings")
data class PostingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "postings_seq")
    var postingId: Long = 0,
    val orderId: Long,
    var postingStatus: PostingStatus = PostingStatus.AWAITING_PAYMENT,
    var currency: String = CurrencyCode.UNDEFINED.name,
    var price: BigDecimal = BigDecimal.ZERO,
)

@Entity(name = "items")
data class ItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "items_seq")
    override var itemId: Long = 0,
    val postingId: Long,
    override val skuId: Int,
    override val qty: Int,
    private val _originalPrice: BigDecimal,
    private val _originalCurrency: String,
    var _price: BigDecimal = BigDecimal.ZERO,
    var _currency: String = CurrencyCode.UNDEFINED.name,
) : Item {

    override val originalPrice: CurrencyPrice
        get() = CurrencyPrice(_originalPrice, _originalCurrency)
    override val price: CurrencyPrice
        get() = CurrencyPrice(_price, _currency)
}

enum class OrderStatus {
    CREATED,
    IN_PROCESS,
    IN_DELIVERY,
    AWAITING_IN_PICKUP,
    RECEIVED,
    CANCELLED
}

enum class PostingStatus {
    AWAITING_PAYMENT,
    IN_PROCESS,
    IN_DELIVERY,
    AWAITING_IN_PICKUP,
    RECEIVED,
    CANCELLED
}