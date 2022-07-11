package ru.spb.ivsamokhvalov.example.demo.camunda.service

import java.math.BigDecimal
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.PostingEntity

interface Order {
    val orderId: Long?
    val orderStatus: OrderStatus
}

interface Posting {
    val postingId: Long
    val orderId: Long
    val currency: CurrencyPrice
    val items: List<Item>
    val postingStatus: PostingStatus
}

data class StubPosting(
    override val postingId: Long,
    override val orderId: Long,
    override val currency: CurrencyPrice,
    override val items: List<Item>,
    override val postingStatus: PostingStatus,
) : Posting {
    constructor(entity: PostingEntity) : this(
        entity.postingId!!,
        entity.orderId,
        CurrencyPrice(entity.price ?: BigDecimal.ZERO, entity.currency),
        emptyList(),
        entity.postingStatus
    )
}

interface Item {
    val itemId: Long?
    val skuId: Int
    val qty: Int
    val price: BigDecimal

}

data class StubItem(
    override val itemId: Long?,
    override val skuId: Int,
    override val qty: Int,
    override val price: BigDecimal,
) : Item

data class OrderWithPosting(
    val order: Order,
    val postings: List<Posting>,
)

enum class CurrencyCode {
    UNDEFINED,
    RUB,
    USD,
    EUR,
    AUD,
    BYN;

    companion object {
        val valuesMap: Map<String, CurrencyCode> = values().associateBy { it.name }
    }
}

data class CreateOrderRequest(
    val postings: Collection<CreatePostingRequest>,
)

data class CreatePostingRequest(
    val items: Collection<CreateItemsRequest>,
    val currencyCode: CurrencyCode,
)

data class CreateItemsRequest(
    val skuId: Int,
    val price: BigDecimal,
    val qty: Int,
)

data class CurrencyPrice(
    val price: BigDecimal,
    val currency: CurrencyCode,
)

data class UpdateOrderRequest(
    val orderId: Long,
    val currency: CurrencyPrice? = null,
    val orderStatus: OrderStatus? = null,
)

data class UpdatePostingRequest(
    val postingId: Long,
    val currency: CurrencyPrice? = null,
    val status: PostingStatus? = null,
)

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