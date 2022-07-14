package ru.spb.ivsamokhvalov.example.demo.camunda.delegate

import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants
import ru.spb.ivsamokhvalov.example.demo.camunda.service.DomainService
import ru.spb.ivsamokhvalov.example.demo.camunda.service.OrderStatus

@Component("recalculateOrderStatusDelegate")
class RecalculateOrderStatusDelegate(
    private val domainService: DomainService,
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val orderId = execution.variables.getValue(CamundaConstants.ORDER_ID) as Long
        val newStatus = domainService.recalculateOrderStatus(orderId)
        logger.debug { "newStatus: ${newStatus}" }
        execution.setVariable(CamundaConstants.NEXT_ORDER_STATUS, newStatus.name)
    }

    private companion object : KLogging()

}


@Component("changeOrderStatusDelegate")
class ChangeOrderStatusDelegate(
    private val domainService: DomainService,
) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val orderId = execution.variables.getValue(CamundaConstants.ORDER_ID) as Long
        val newStatus = OrderStatus.valueOf(execution.variables.getValue(CamundaConstants.NEXT_ORDER_STATUS) as String)
        domainService.updateOrderStatus(orderId, newStatus)
        execution.setVariable(CamundaConstants.CURRENT_ORDER_STATUS, newStatus.name)
    }
}


@Component("recalculateOrderPriceDelegate")
class RecalculateOrderPriceDelegate(
    private val domainService: DomainService,
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val orderId = execution.variables.getValue(CamundaConstants.ORDER_ID) as Long
        domainService.recalculateOrderPrice(orderId)
    }
}

@Component("cancelAllPostingsDelegate")
class CancelAllPostingsDelegate(
    private val domainService: DomainService,
    private val runtimeService: RuntimeService,
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        val orderId = execution.variables.getValue(CamundaConstants.ORDER_ID) as Long
        val order = domainService.getOrder(orderId)
        order.postings.onEach { posting ->
            val businessKey = CamundaConstants.buildPostingProcessBusinessKey(posting.postingId)
            runtimeService.createMessageCorrelation(CamundaConstants.POSTING_CANCELLED)
                .processInstanceBusinessKey(businessKey)
                .correlateAll()
        }
    }
}
