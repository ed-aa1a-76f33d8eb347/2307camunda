package ru.spb.ivsamokhvalov.example.demo.camunda.service

import mu.KLogging
import org.springframework.stereotype.Service
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.OrderStatus
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.PostingStatus

interface DomainService {

    fun createOrder(request: CreateOrderRequest): OrderWithPosting

    fun getOrder(orderId: Long): OrderWithPosting

    fun getPosting(postingId: Long): Posting

    fun updateOrderStatus(orderId: Long, orderStatus: OrderStatus)

    fun updatePostingStatus(postingId: Long, postingStatus: PostingStatus)

    fun recalculateOrderStatus(orderId: Long): OrderStatus

    fun recalculateOrderPrice(orderId: Long)

    fun recalculatePostingPrice(postingId: Long)

    fun cancelPosting(postingId: Long)
}


@Service
class DomainServiceImpl(
    private val orderService: OrderService,
    private val postingService: PostingService,
    private val itemService: PostingItemService,
    private val camundaService: CamundaService,
    private val currenciesConverter: CurrenciesConverterService,
) : DomainService {
    override fun createOrder(request: CreateOrderRequest): OrderWithPosting {
        val order = orderService.createOrder(request)
        postingService.createPostings(order.orderId, request.postings)
        val result = getOrder(order.orderId)
        result.postings.onEach {
            camundaService.startPostingProcess(it)
        }
        camundaService.startOrderProcess(result.order)
        return result
    }

    override fun getOrder(orderId: Long): OrderWithPosting {
        val order = orderService.getOrder(orderId)
        val postings = postingService.getPostingsByOrderId(orderId)
        return OrderWithPosting(order, postings)
    }

    override fun getPosting(postingId: Long) = postingService.getPosting(postingId)

    override fun recalculateOrderStatus(orderId: Long): OrderStatus {
        val postings = postingService.getPostingsByOrderId(orderId)
        val result = calculateOrderStatus(postings)
        logger.debug { "newStatus: $result, postingStatuses: ${postings.map { it.postingStatus }}" }
        return result
    }

    override fun recalculateOrderPrice(orderId: Long) {
        val postings = postingService.getPostingsByOrderId(orderId)
        val distinctCurrencies =
            postings.filter { it.postingStatus != PostingStatus.CANCELLED }.map { it.currency.currency }.distinct()
        require(distinctCurrencies.size <= 1) {
            "Перед пересчетом стоимости заказа, все валюты постингов должны быть сделаны одинаковыми"
        }
        val orderPrice = postings.filter { it.postingStatus != PostingStatus.CANCELLED }.sumOf { it.currency.price }
        orderService.updateOrder(
            UpdateOrderRequest(
                orderId = orderId,
                currency = CurrencyPrice(orderPrice, distinctCurrencies.singleOrNull() ?: CurrencyCode.UNDEFINED)
            )
        )
    }

    override fun recalculatePostingPrice(postingId: Long) {
        val posting = postingService.getPosting(postingId)
        posting.items.onEach { item ->
            val convertedPrice = currenciesConverter.convertToDefault(item.originalPrice)
            itemService.updateItem(UpdateItemRequest(itemId = item.itemId, currency = convertedPrice))
        }
        val originalPrice = posting.currency
        val convertedPrice = currenciesConverter.convertToDefault(originalPrice)
        if (convertedPrice == originalPrice) {
            logger.info { "Nothing to change for posting: $posting" }
            return
        }
        postingService.updatePosting(UpdatePostingRequest(postingId = postingId, currency = convertedPrice))

    }

    override fun cancelPosting(postingId: Long) {
        val posting = postingService.getPosting(postingId)
        if (posting.postingStatus == PostingStatus.CANCELLED) return
        postingService.updatePosting(
            UpdatePostingRequest(
                postingId = postingId,
                postingStatus = PostingStatus.CANCELLED
            )
        )
    }

    override fun updateOrderStatus(orderId: Long, orderStatus: OrderStatus) {
        val order = orderService.getOrder(orderId)
        if (order.orderStatus == orderStatus) return
        orderService.updateOrder(UpdateOrderRequest(orderId = orderId, orderStatus = orderStatus))
    }

    override fun updatePostingStatus(postingId: Long, postingStatus: PostingStatus) {
        val posting = postingService.getPosting(postingId)
        if (posting.postingStatus == postingStatus) return
        postingService.updatePosting(UpdatePostingRequest(postingId = postingId, postingStatus = postingStatus))
    }

    private fun calculateOrderStatus(postings: List<Posting>): OrderStatus {
        val statuses = postings.map { it.postingStatus }.distinct()
        return when {
            statuses.all { it == PostingStatus.CANCELLED } -> OrderStatus.CANCELLED
            statuses.all { it in setOf(PostingStatus.CANCELLED, PostingStatus.RECEIVED) } -> OrderStatus.RECEIVED
            statuses.any { it == PostingStatus.AWAITING_IN_PICKUP } -> OrderStatus.AWAITING_IN_PICKUP
            statuses.any { it == PostingStatus.IN_DELIVERY } -> OrderStatus.IN_DELIVERY
            statuses.any { it == PostingStatus.IN_DELIVERY } -> OrderStatus.IN_DELIVERY
            statuses.any { it == PostingStatus.IN_PROCESS } -> OrderStatus.IN_PROCESS
            statuses.any { it == PostingStatus.AWAITING_PAYMENT } -> OrderStatus.CREATED
            else -> throw IllegalArgumentException("Невозможно высчитать статус ордера")
        }
    }

    private companion object : KLogging()
}