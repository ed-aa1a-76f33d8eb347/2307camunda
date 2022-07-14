package ru.spb.ivsamokhvalov.example.demo.camunda.service

import org.springframework.stereotype.Service
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.OrderEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.OrderRepository

interface OrderService {
    fun createOrder(request: CreateOrderRequest): Order
    fun getOrder(orderId: Long): Order
    fun updateOrder(request: UpdateOrderRequest): Order
}

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
) : OrderService {

    override fun createOrder(request: CreateOrderRequest): Order = orderRepository.save(OrderEntity())

    override fun getOrder(orderId: Long): Order = orderRepository.findById(orderId).get()

    override fun updateOrder(request: UpdateOrderRequest): Order {
        val order = orderRepository.findById(request.orderId).get()
        request.orderStatus?.let { newStatus ->
            order.orderStatus = newStatus
        }
        request.currency?.let { newPrice ->
            order.price = newPrice.price
            order.currency = newPrice.currency
        }
        return orderRepository.save(order)
    }
}