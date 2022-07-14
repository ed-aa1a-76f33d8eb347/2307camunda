package ru.spb.ivsamokhvalov.example.demo.camunda.controller

import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.ORDER_CANCELLED
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_CANCELLED
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_IN_DELIVERY
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_IN_PICKUP
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_IS_PAID
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_RECEIVED
import ru.spb.ivsamokhvalov.example.demo.camunda.service.PostingService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.PostingStatus

@RestController
@RequestMapping("/notify")
class NotifyController(
    private val postingService: PostingService,
    private val runtimeService: RuntimeService,
) {

    //    @PostMapping("/posting/{postingId}/inProcess")
    fun markPostingInProcess(@PathVariable postingId: Long) {
        pushMessageToProcess(postingId, POSTING_IS_PAID)
    }

    //    @PostMapping("/posting/{postingId}/inDelivery")
    fun markPostingInDelivery(@PathVariable postingId: Long) {
        pushMessageToProcess(postingId, POSTING_IN_DELIVERY)
    }

    //    @PostMapping("/posting/{postingId}/awaiting")
    fun markPostingAwaiting(@PathVariable postingId: Long) {
        pushMessageToProcess(postingId, POSTING_IN_PICKUP)
    }

    //    @PostMapping("/posting/{postingId}/received")
    fun markPostingReceived(@PathVariable postingId: Long) {
        pushMessageToProcess(postingId, POSTING_RECEIVED)
    }

    @PostMapping("/posting/{postingId}/canceled")
    fun markPostingCanceled(@PathVariable postingId: Long) {
        pushMessageToProcess(postingId, POSTING_CANCELLED)
    }

    @PostMapping("/posting/{postingId}/nextStatus")
    fun movePostingToNextStatus(@PathVariable postingId: Long) {
        logger.debug { "postingId: $postingId" }
        val posting = postingService.getPosting(postingId)
        when (posting.postingStatus) {
            PostingStatus.AWAITING_PAYMENT -> markPostingInProcess(postingId)
            PostingStatus.IN_PROCESS -> markPostingInDelivery(postingId)
            PostingStatus.IN_DELIVERY -> markPostingAwaiting(postingId)
            PostingStatus.AWAITING_IN_PICKUP -> markPostingReceived(postingId)
            PostingStatus.RECEIVED -> {}
            PostingStatus.CANCELLED -> {}
        }
    }

    @PostMapping("/order/{orderId}/canceled")
    fun cancelOrder(@PathVariable orderId: Long) {
        val businessKey = CamundaConstants.buildOrderProcessBusinessKey(orderId)
        runtimeService.createMessageCorrelation(ORDER_CANCELLED)
            .processInstanceBusinessKey(businessKey)
            .correlateAll()
    }

    private fun pushMessageToProcess(postingId: Long, messageCorrelation: String) {
        val businessKey = CamundaConstants.buildPostingProcessBusinessKey(postingId)
        runtimeService.createMessageCorrelation(messageCorrelation)
            .processInstanceBusinessKey(businessKey)
            .correlateAll()

    }

    private companion object : KLogging()

}
