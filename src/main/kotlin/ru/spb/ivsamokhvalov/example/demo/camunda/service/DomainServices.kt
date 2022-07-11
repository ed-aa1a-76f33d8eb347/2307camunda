package ru.spb.ivsamokhvalov.example.demo.camunda.service

import java.math.BigDecimal
import mu.KLogging
import org.springframework.stereotype.Service
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.ItemEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.ItemRepository
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.OrderEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.OrderRepository
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.PostingEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.PostingRepository

interface MainService {
    fun createOrder(request: CreateOrderRequest): OrderWithPosting
    fun getOrder(orderId: Long): OrderWithPosting

//    fun updateOrder(request: UpdateOrderRequest): Order

    fun recalculateOrderStatus(orderId: Long)

    fun recalculateOrderPrice(orderId: Long)
}



@Service
class MainServiceImpl(
    private val orderService: OrderService,
    private val postingService: PostingService,
) : MainService {
    override fun createOrder(request: CreateOrderRequest): OrderWithPosting {
        val order = orderService.createOrder(request)
        postingService.createPostings(order.orderId, request.postings)
        return getOrder(order.orderId)
    }

    override fun getOrder(orderId: Long): OrderWithPosting {
        val order = orderService.getOrder(orderId)
        val postings = postingService.getPostingByOrderId(orderId)
        return OrderWithPosting(order, postings)
    }

    override fun recalculateOrderStatus(orderId: Long) {
        val order = orderService.getOrder(orderId)
        val postings = postingService.getPostingByOrderId(orderId)
        val newStatus: OrderStatus = calculateOrderStatus(postings)
        if (newStatus == order.orderStatus) return
        orderService.updateOrder(UpdateOrderRequest(orderId = orderId, orderStatus = newStatus))
    }

    override fun recalculateOrderPrice(orderId: Long) {
        val order = orderService.getOrder(orderId)
        val postings = postingService.getPostingByOrderId(orderId)
        val distinctCurrencies = postings.filter { it.postingStatus != PostingStatus.CANCELLED }.map { it.currency.currency }.distinct()
        require(distinctCurrencies.size == 1) {
            "Перед пересчетом стоимости заказа, все валюты постингов должны быть сделаны одинаковыми"
        }
        val orderPrice = postings.filter { it.postingStatus != PostingStatus.CANCELLED }.sumOf { it.currency.price }
        orderService.updateOrder(UpdateOrderRequest(orderId = orderId, currency = CurrencyPrice(orderPrice, distinctCurrencies.single())))
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
}