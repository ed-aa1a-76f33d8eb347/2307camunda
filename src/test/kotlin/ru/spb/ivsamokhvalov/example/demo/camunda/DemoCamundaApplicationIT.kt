package ru.spb.ivsamokhvalov.example.demo.camunda

import mu.KLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.CreateRandomPostingRequest
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.NotifyController
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.OrderController
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.OrderStatus
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.PostingStatus

@SpringBootTest
class DemoCamundaApplicationIT {

    @Autowired
    lateinit var orderController: OrderController

    @Autowired
    lateinit var notifyController: NotifyController

    @Test
    fun testHappyPath() {
        val orderId = orderController.createRandom(CreateRandomPostingRequest(postingCount = 1)).order.orderId
        val order = orderController.getOrder(orderId).also {
            logger.debug { "order: ${it}" }
        }
        val posting = order.postings.first()
        assertEquals(PostingStatus.AWAITING_PAYMENT, posting.postingStatus)
        assertEquals(OrderStatus.CREATED, order.order.orderStatus)
        notifyController.movePostingToNextStatus(posting.postingId)
        assertStatuses(orderId, OrderStatus.IN_PROCESS, PostingStatus.IN_PROCESS)
        notifyController.movePostingToNextStatus(posting.postingId)
        assertStatuses(orderId, OrderStatus.IN_DELIVERY, PostingStatus.IN_DELIVERY)
        notifyController.movePostingToNextStatus(posting.postingId)
        assertStatuses(orderId, OrderStatus.AWAITING_IN_PICKUP, PostingStatus.AWAITING_IN_PICKUP)
        notifyController.movePostingToNextStatus(posting.postingId)
        assertStatuses(orderId, OrderStatus.RECEIVED, PostingStatus.RECEIVED)

        orderController.getOrder(orderId).also {
            logger.debug { "order: ${it}" }
        }
    }

    @Test
    fun testCancelOrder() {
        val orderId = orderController.createRandom(null).order.orderId
        notifyController.cancelOrder(orderId)
        val order = orderController.getOrder(orderId).also {
            logger.debug { "order: ${it}" }
        }
        order.postings.onEach { assertEquals(PostingStatus.CANCELLED, it.postingStatus) }
        assertEquals(OrderStatus.CANCELLED, order.order.orderStatus)

    }

    private fun assertStatuses(orderId: Long, orderStatus: OrderStatus, postingStatus: PostingStatus) {
        val order = orderController.getOrder(orderId)
        assertEquals(orderStatus, order.order.orderStatus)
        val posting = order.postings.first()
        assertEquals(postingStatus, posting.postingStatus)
    }

    private companion object : KLogging()

}
