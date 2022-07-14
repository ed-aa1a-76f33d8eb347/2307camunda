//package ru.spb.ivsamokhvalov.example.demo.camunda
//
//import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
//import org.camunda.bpm.engine.test.Deployment
//import org.camunda.bpm.engine.test.ProcessEngineRule
//import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
//import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat
//import org.camunda.bpm.engine.test.mock.Mocks
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.junit.MockitoJUnitRunner
//import org.mockito.kotlin.mock
//import ru.spb.ivsamokhvalov.example.demo.camunda.CamundaConstants.POSTING_PROCESS_KEY
//import ru.spb.ivsamokhvalov.example.demo.camunda.controller.NotifyController
//import ru.spb.ivsamokhvalov.example.demo.camunda.delegate.ChangePostingStatusDelegate
//import ru.spb.ivsamokhvalov.example.demo.camunda.delegate.NotifyOrderRecalculateDelegate
//import ru.spb.ivsamokhvalov.example.demo.camunda.delegate.RecalculateCurrencyDelegate
//import ru.spb.ivsamokhvalov.example.demo.camunda.repo.PostingEntity
//
////@SpringBootTest
////@ExtendWith(SpringExtension::class)
//@RunWith(MockitoJUnitRunner::class)
//class DemoCamundaApplicationTestJunit4 {
//
//    @get:Rule
//    var processEngineRule = ProcessEngineRule(ProcessTestConfig.processEngine)
//
//    private val notifyController = NotifyController(mock(), processEngineRule.processEngine.runtimeService)
//
//    private val posting: PostingEntity
//        get() = PostingEntity(1, 10)
//
//
//    @Before
//    fun setUp() {
//        Mocks.register("recalculateCurrencyDelegate", mock<RecalculateCurrencyDelegate>())
//        Mocks.register("changePostingStatusDelegate", mock<ChangePostingStatusDelegate>())
//        Mocks.register("notifyOrderRecalculateDelegate", mock<NotifyOrderRecalculateDelegate>())
//    }
//
//    @After
//    fun teardown() {
//        Mocks.reset()
//    }
//
//
//    @Test
//    @Deployment(resources = ["bpmn/PostingProcess.bpmn"])
//    fun testCancelPosting() {
//        val businessKey = CamundaConstants.buildPostingProcessBusinessKey(postingId = posting.postingId)
//        val processInstance = processEngineRule.processEngine.runtimeService
//            .startProcessInstanceByKey(
//                POSTING_PROCESS_KEY,
//                businessKey,
//                BpmnAwareTests.withVariables(
//                    CamundaConstants.POSTING_ID, posting.postingId,
//                    CamundaConstants.AWAITING_PAYMENT, "PT1M",
//                    CamundaConstants.AWAITING_RECEIVED, "PT1M",
//                    CamundaConstants.ORDER_ID, posting.orderId
//                )
//            )
//
//        assertThat(processInstance).isNotNull
//        assertThat(processInstance).isStarted
//        assertThat(processInstance).isWaitingAt("Gateway_0lobnkh")
//        processEngineRule.processEngine.runtimeService.createMessageCorrelation(CamundaConstants.POSTING_CANCELLED)
//            .processInstanceBusinessKey(businessKey)
//            .correlateAll()
//        assertThat(processInstance).isEnded
//        println(processInstance)
////        notifyController.markPostingCanceled(postingId = posting.postingId)
////        assertThat(processInstance).isEnded
//
//    }
////    @Autowired
////    lateinit var processEngine: ProcessEngine
////
////    @Autowired
////    lateinit var orderController: OrderController
////
////    @Autowired
////    lateinit var notifyController: NotifyController
//
//
////    @Test
////    fun testCancelPosting() {
////        val order = orderController.createRandom(null)
////        val posting = order.postings.first()
////        val businessKey = CamundaConstants.buildPostingProcessBusinessKey(postingId = posting.postingId)
////        val processInstance =
////            processEngine.runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).list()
////                .single()
////        assertThat(processInstance).isActive
////        assertThat(processInstance).isWaitingAt("Gateway_0lobnkh")
////        notifyController.markPostingCanceled(postingId = posting.postingId)
////        assertThat(processInstance).isEnded
////    }
//
//
//}
//
//
//class ProcessTestConfig {
//    companion object {
//        val processEngine = StandaloneInMemProcessEngineConfiguration
//            .createStandaloneInMemProcessEngineConfiguration()
//            .buildProcessEngine()
//    }
//}
