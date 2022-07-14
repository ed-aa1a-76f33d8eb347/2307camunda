package ru.spb.ivsamokhvalov.example.demo.camunda

object CamundaConstants {
    //posting
    const val POSTING_PROCESS_KEY = "CreatePostingProcessId"

    //variables
    const val POSTING_ID = "postingId"
    const val POSTING_STATUS = "postingStatus"
    const val AWAITING_PAYMENT = "awaitingPayment"
    const val AWAITING_RECEIVED = "awaitingReceived"

    //messageIds
    const val POSTING_IS_PAID = "postingIsPaid"
    const val POSTING_IN_DELIVERY = "postingInDelivery"
    const val POSTING_IN_PICKUP = "postingInPickup"
    const val POSTING_RECEIVED = "postingReceived"
    const val POSTING_CANCELLED = "postingCancelled"
    const val ORDER_CANCELLED = "orderCancelled"

    //order
    const val ORDER_PROCESS_KEY = "CreateOrderProcessId"

    //variables
    const val ORDER_ID = "orderId"
    const val CURRENT_ORDER_STATUS = "currentStatus"
    const val NEXT_ORDER_STATUS = "nextStatus"

    //messageIds
    const val RECALCULATE_ORDER_STATUS = "recalculateOrderStatus"

    fun buildPostingProcessBusinessKey(postingId: Long) = "posting_${postingId}"
    fun buildOrderProcessBusinessKey(postingId: Long) = "order_${postingId}"


}