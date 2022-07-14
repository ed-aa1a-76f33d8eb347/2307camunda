package ru.spb.ivsamokhvalov.example.demo.camunda.service

import java.math.BigDecimal
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.OrderStatus
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.PostingEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.PostingStatus

interface Order {
    val orderId: Long
    val orderStatus: OrderStatus
}

interface Posting {
    val postingId: Long
    val orderId: Long
    val currency: CurrencyPrice
    val items: List<Item>
    val postingStatus: PostingStatus
}

data class PostingDto(
    override val postingId: Long,
    override val orderId: Long,
    override val currency: CurrencyPrice,
    override val items: List<Item>,
    override val postingStatus: PostingStatus,
) : Posting {
    constructor(entity: PostingEntity, items: List<Item>) : this(
        entity.postingId,
        entity.orderId,
        CurrencyPrice(entity.price, entity.currency),
        items,
        entity.postingStatus
    )

    constructor(entity: PostingEntity) : this(entity, emptyList())

}

interface Item {
    val itemId: Long?
    val skuId: Int
    val qty: Int
    val price: BigDecimal

}

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

class CurrencyPrice(
    val price: BigDecimal,
    val currency: CurrencyCode,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CurrencyPrice

        if (currency != other.currency) return false
        return price.compareTo(other.price) == 0
    }

    override fun hashCode(): Int {
        var result = price.hashCode()
        result = 31 * result + currency.hashCode()
        return result
    }

    override fun toString() = "CurrencyPrice(price: $price, currency: $currency)"
}


data class UpdateOrderRequest(
    val orderId: Long,
    val currency: CurrencyPrice? = null,
    val orderStatus: OrderStatus? = null,
)

data class UpdatePostingRequest(
    val postingId: Long,
    val currency: CurrencyPrice? = null,
    val postingStatus: PostingStatus? = null,
)
