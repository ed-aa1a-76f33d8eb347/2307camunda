package ru.spb.ivsamokhvalov.example.demo.camunda.delegate

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.ORDER_ID
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_ID
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_STATUS
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.RECALCULATE_ORDER_STATUS
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.buildOrderProcessBusinessKey
import ru.spb.ivsamokhvalov.example.demo.camunda.service.DomainService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.PostingStatus


@Component("recalculateCurrencyDelegate")
class RecalculateCurrencyDelegate(
    private val domainService: DomainService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val postingId = execution.variables.getValue(POSTING_ID) as Long
        domainService.recalculatePostingPrice(postingId)
    }
}

@Component("changePostingStatusDelegate")
class ChangePostingStatusDelegate(
    private val domainService: DomainService,
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val postingId = execution.variables.getValue(POSTING_ID) as Long
        val newPostingStatus = PostingStatus.valueOf(execution.variables.getValue(POSTING_STATUS) as String)
        domainService.updatePostingStatus(postingId, newPostingStatus)
    }

}

@Component("notifyOrderRecalculateDelegate")
class NotifyOrderRecalculateDelegate(
    private val runtimeService: RuntimeService,
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val orderId = execution.variables.getValue(ORDER_ID) as Long
        val businessKey = buildOrderProcessBusinessKey(orderId)
        runtimeService.createMessageCorrelation(RECALCULATE_ORDER_STATUS)
            .processInstanceBusinessKey(businessKey)
            .correlateAll()
    }
}


