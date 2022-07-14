package ru.spb.ivsamokhvalov.example.demo.camunda.service

import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.AWAITING_PAYMENT
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.AWAITING_RECEIVED
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.CURRENT_ORDER_STATUS
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.NEXT_ORDER_STATUS
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.ORDER_ID
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.ORDER_PROCESS_KEY
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_ID
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_PROCESS_KEY
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.buildOrderProcessBusinessKey
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.buildPostingProcessBusinessKey

interface CamundaService {

    fun startPostingProcess(posting: Posting): ProcessInstance

    fun startOrderProcess(order: Order): ProcessInstance

}


@Service
class CamundaServiceImpl(
    private val runtimeService: RuntimeService,
) : CamundaService {

    @Value("\${timeout.awaitingPayment}")
    private lateinit var awaitingPayment: String

    @Value("\${timeout.awaitingReceived}")
    private lateinit var awaitingReceived: String

    override fun startPostingProcess(posting: Posting): ProcessInstance {
        val businessKey = buildPostingProcessBusinessKey(posting.postingId)
        val process = runtimeService.startProcessInstanceByKey(
            POSTING_PROCESS_KEY,
            businessKey,
            mapOf(
                POSTING_ID to posting.postingId,
                AWAITING_PAYMENT to awaitingPayment,
                AWAITING_RECEIVED to awaitingReceived,
                ORDER_ID to posting.orderId
            )
        )
        logger.debug { "Success start PostingProcess with processInstanceId: ${process.processInstanceId}" }
        return process
    }

    override fun startOrderProcess(order: Order): ProcessInstance {
        val businessKey = buildOrderProcessBusinessKey(order.orderId)
        val process = runtimeService.startProcessInstanceByKey(
            ORDER_PROCESS_KEY,
            businessKey,
            mapOf(
                ORDER_ID to order.orderId,
                CURRENT_ORDER_STATUS to order.orderStatus.name,
                NEXT_ORDER_STATUS to order.orderStatus.name
            )
        )
        logger.debug { "Success start PostingProcess with processInstanceId: ${process.processInstanceId}" }
        return process
    }

    private companion object : KLogging()
}
