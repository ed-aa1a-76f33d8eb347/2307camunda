package ru.spb.ivsamokhvalov.example.demo.camunda.service

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension
import org.camunda.bpm.extension.mockito.DelegateExpressions.autoMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants
import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_PROCESS_KEY
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.PostingEntity


@Deployment(resources = ["bpmn/PostingProcess.bpmn"])
class DemoCamundaApplicationTest {

    @RegisterExtension
    val extension = ProcessEngineExtension.builder()
        .useProcessEngine(
            StandaloneInMemProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration()
                .buildProcessEngine()
        )
        .build()

    @BeforeEach
    fun setUp() {
        autoMock("bpmn/PostingProcess.bpmn")
    }

    private val runtimeService: RuntimeService by lazy { runtimeService() }
    private val posting: PostingEntity
        get() = PostingEntity(1, 10)


    @Test
    fun testCancelPosting() {
        val businessKey = CamundaConstants.buildPostingProcessBusinessKey(postingId = posting.postingId)
        val processInstance = runtimeService
            .startProcessInstanceByKey(
                POSTING_PROCESS_KEY,
                businessKey,
                BpmnAwareTests.withVariables(
                    CamundaConstants.POSTING_ID, posting.postingId,
                    CamundaConstants.AWAITING_PAYMENT, "PT1M",
                    CamundaConstants.AWAITING_RECEIVED, "PT1M",
                    CamundaConstants.ORDER_ID, posting.orderId
                )
            )
        assertThat(processInstance).isStarted
        assertThat(processInstance).hasPassed("Activity_15gh2i6")
        assertThat(processInstance).isWaitingAt("Gateway_0lobnkh")
        runtimeService.createMessageCorrelation(CamundaConstants.POSTING_CANCELLED)
            .processInstanceBusinessKey(businessKey)
            .correlateAll()
        assertThat(processInstance).hasPassed("Activity_10a9d55")
        assertThat(processInstance).isEnded
    }

}
