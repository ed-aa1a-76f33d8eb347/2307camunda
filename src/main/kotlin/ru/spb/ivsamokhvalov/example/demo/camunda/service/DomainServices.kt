package ru.spb.ivsamokhvalov.example.demo.camunda.service

import java.math.BigDecimal
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
    fun getPosting(postingId: Long): Posting

    fun updateOrder(request: UpdateOrderRequest): Order
    fun updatePosting(request: UpdatePostingRequest): Posting
}


//interface OrderService {
//    fun createOrder(): Order
//    fun getOrder(orderId: Long): OrderWithPosting
//    fun updateOrder(request: UpdateOrderRequest): Order
//}
//
//interface PostingService {
//    fun createPosting(): Posting
//    fun getPosting(postingId: Long): Posting
//    fun updatePosting(request: UpdatePostingRequest): Posting
//}


@Service
class MainServiceImpl(
    private val orderRepository: OrderRepository,
    private val postingRepository: PostingRepository,
    private val itemRepository: ItemRepository,
) : MainService {
    override fun createOrder(request: CreateOrderRequest): OrderWithPosting {
        val order = orderRepository.save(OrderEntity())
        request.postings.onEach {
            val price = it.items.sumOf { item -> item.price * BigDecimal(item.qty) }
            val posting = postingRepository.save(PostingEntity(orderId = order.orderId!!, price = price, currency = it.currencyCode))
            it.items.onEach { p ->
                itemRepository.save(
                    ItemEntity(
                        skuId = p.skuId,
                        qty = p.qty,
                        price = p.price,
                        postingId = posting.postingId!!
                    )
                )
            }
        }
        return getOrder(order.orderId!!)
    }

    override fun getOrder(orderId: Long): OrderWithPosting {
        val order = orderRepository.findById(orderId).get()
        val postings = postingRepository.findByOrderIdOrderByPostingIdAsc(orderId)
        return OrderWithPosting(order, postings.map { getPosting(it.postingId!!) })
    }

    override fun getPosting(postingId: Long): Posting {
        val posting = postingRepository.findById(postingId).get()

        return StubPosting(
            postingId = posting.postingId!!,
            orderId = posting.orderId,
            items = getItemsByPostingId(posting.postingId),
            currency = CurrencyPrice(posting.price, posting.currency),
            postingStatus = posting.postingStatus
        )
    }

    fun getItemsByPostingId(postingId: Long): List<Item> {
        val items = itemRepository.findByPostingIdOrderByItemIdAsc(postingId)
        return items.map { StubItem(itemId = it.itemId, skuId = it.skuId, qty = it.qty, price = it.price) }
    }

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

    override fun updatePosting(request: UpdatePostingRequest): Posting {
        val posting = postingRepository.findById(request.postingId).get()
        request.status?.let { newStatus ->
            posting.postingStatus = newStatus
        }
        request.currency?.let { newPrice ->
            posting.currency = newPrice.currency
            posting.price = newPrice.price
        }
        return StubPosting(postingRepository.save(posting))
    }
}