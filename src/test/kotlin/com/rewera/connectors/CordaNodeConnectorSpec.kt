package com.rewera.connectors

import com.rewera.models.api.RpcStartFlowRequestParameters
import com.rewera.testdata.TestData.MultipleParametersTestFlow
import com.rewera.testdata.TestData.SingleParameterTestFlow
import com.rewera.testdata.TestData.TestFlowResult
import com.rewera.testdata.TestData.flowHandleWithClientId
import com.rewera.testdata.TestData.testClientId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.future.await
import net.corda.core.flows.StateMachineRunId
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowHandleWithClientIdImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.*

class CordaNodeConnectorSpec {

    private val cordaRpcOpsFactory = Mockito.mock(CordaRpcOpsFactory::class.java)
    private val rpcOps = Mockito.mock(CordaRPCOps::class.java)
    private val parametersExtractor = Mockito.mock(FlowClassConstructorParametersExtractor::class.java)
    private val flowClassBuilder = Mockito.mock(FlowClassBuilder::class.java)

    private val cordaNodeConnector =
        CordaNodeConnector(cordaRpcOpsFactory, parametersExtractor, flowClassBuilder)

    @BeforeEach
    fun setup() {
        reset(cordaRpcOpsFactory, rpcOps, parametersExtractor, flowClassBuilder)
        whenever(cordaRpcOpsFactory.rpcOps).thenReturn(rpcOps)
    }

    @Nested
    @DisplayName("CordaNodeConnector on getRegisteredFlows")
    inner class GetRegisteredFlowsSpec {

        @Test
        fun `should call CordaRPCOps`() {
            whenever(rpcOps.registeredFlows()).thenReturn(emptyList())

            cordaNodeConnector.getRegisteredFlows()

            verify(rpcOps).registeredFlows()
        }

        @Test
        fun `should return empty list when CordaRPCOps returns empty list`() {
            whenever(rpcOps.registeredFlows()).thenReturn(emptyList())

            cordaNodeConnector.getRegisteredFlows() shouldBe emptyList()
        }

        @Test
        fun `should return the value from CordaRPCOps when it returns non-empty list`() {
            val flows = listOf("test.flow.name")
            whenever(rpcOps.registeredFlows()).thenReturn(flows)

            cordaNodeConnector.getRegisteredFlows() shouldBe flows
        }
    }

    @Nested
    @DisplayName("CordaNodeConnector on getFlowOutcomeForClientId")
    inner class GetFlowOutcomeForClientIdSpec {

        private val testReturnValue = TestFlowResult("Test value", 1234567)

        @Test
        suspend fun `should call CordaRPCOps`() {
            whenever(rpcOps.reattachFlowWithClientId<TestFlowResult>(any()))
                .thenReturn(flowHandleWithClientId(testClientId, testReturnValue))

            cordaNodeConnector.getFlowOutcomeForClientId<TestFlowResult>(testClientId)!!.await()

            verify(rpcOps).reattachFlowWithClientId<TestFlowResult>(eq(testClientId))
        }

        @Test
        suspend fun `should return Future with value returned by CordaRPCOps`() {
            whenever(rpcOps.reattachFlowWithClientId<TestFlowResult>(any()))
                .thenReturn(flowHandleWithClientId(testClientId, testReturnValue))

            val result = cordaNodeConnector.getFlowOutcomeForClientId<TestFlowResult>(testClientId)!!.await()

            result shouldBe testReturnValue
        }
    }

    @Nested
    @DisplayName("CordaNodeConnector on startFlowTyped")
    inner class StartFlowTypedSpec {

        private val singleParameterFlowName = "com.rewera.testdata.TestData\$SingleParameterTestFlow"
        private val multipleParametersFlowName = "com.rewera.testdata.TestData\$MultipleParametersTestFlow"
        private val flowIdValue = UUID.randomUUID()
        private val someParameterValue = "Test param value"
        private val flowHandle =
            FlowHandleWithClientIdImpl(StateMachineRunId(flowIdValue), doneFuture("Result"), testClientId)
        private val flowParams = RpcStartFlowRequestParameters("{\"someParameter\":\"$someParameterValue\"}")

        @Test
        fun `should call FlowClassBuilder`() {
            whenever(flowClassBuilder.buildFlowClass(any())).thenReturn(SingleParameterTestFlow::class.java)
            whenever(flowClassBuilder.buildFlowClass(any())).thenReturn(SingleParameterTestFlow::class.java)
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)

            cordaNodeConnector.startFlowTyped(testClientId, singleParameterFlowName, flowParams)

            verify(flowClassBuilder).buildFlowClass(eq(singleParameterFlowName))
        }

        @Test
        fun `should call CordaRPCOps`() {
            whenever(flowClassBuilder.buildFlowClass(any())).thenReturn(SingleParameterTestFlow::class.java)
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)
            whenever(parametersExtractor.extractParameters<Any>(any(), any())).thenReturn(listOf(someParameterValue))

            cordaNodeConnector.startFlowTyped(testClientId, singleParameterFlowName, flowParams)

            verify(rpcOps).startFlowDynamicWithClientId(
                eq(testClientId),
                eq(SingleParameterTestFlow::class.java),
                eq(someParameterValue)
            )
        }

        @Test
        fun `should return RpcStartFlowResponse with flowId returned from CordaRPCOps`() {
            whenever(flowClassBuilder.buildFlowClass(any())).thenReturn(SingleParameterTestFlow::class.java)
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)
            whenever(parametersExtractor.extractParameters<Any>(any(), any())).thenReturn(listOf(someParameterValue))

            val result = cordaNodeConnector.startFlowTyped(testClientId, singleParameterFlowName, flowParams)

            result.clientId shouldBe testClientId
            result.flowId.uuid shouldBe flowIdValue
        }

        @Test
        fun `when FlowClassBuilder throws ClassNotFoundException should throw the same exception`() {
            whenever(flowClassBuilder.buildFlowClass(any())).thenThrow(RuntimeException("Test Exception"))

            val exc = shouldThrow<RuntimeException> {
                cordaNodeConnector.startFlowTyped(
                    testClientId,
                    "invalid.class.name.but.it.does.not.matter.here",
                    flowParams
                )
            }
            exc.message shouldBe "Test Exception"
        }

        @Test
        fun `when provided with flow that has multiple constructor params should call CordaRPCOps with params obtained from parametersExtractor`() {
            whenever(flowClassBuilder.buildFlowClass(any())).thenReturn(MultipleParametersTestFlow::class.java)
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)
            val paramExtractorReturnedValues = listOf("Test value 1", 1234567, "Test value 3")
            whenever(parametersExtractor.extractParameters<Any>(any(), any())).thenReturn(paramExtractorReturnedValues)

            val multipleFlowParams = RpcStartFlowRequestParameters(
                "{\"firstParameter\":\"Test value 1\", \"secondParameter\":1234567, \"thirdParameter\":\"Test value 3\"}"
            )

            cordaNodeConnector.startFlowTyped(testClientId, multipleParametersFlowName, multipleFlowParams)

            val expectedVararg = paramExtractorReturnedValues.toTypedArray()
            verify(rpcOps).startFlowDynamicWithClientId(
                testClientId,
                MultipleParametersTestFlow::class.java,
                *expectedVararg
            )
        }
    }

    @Nested
    @DisplayName("CordaNodeConnector on killFlow")
    inner class KillFlowSpec {

        private val flowIdValue = UUID.randomUUID()

        @Test
        fun `should call CordaRPCOps`() {
            whenever(rpcOps.killFlow(any())).thenReturn(false)

            cordaNodeConnector.killFlow(flowIdValue)

            verify(rpcOps).killFlow(eq(StateMachineRunId(flowIdValue)))
        }

        @Test
        fun `should return value from CordaRPCOps`() {
            whenever(rpcOps.killFlow(any())).thenReturn(true)

            cordaNodeConnector.killFlow(flowIdValue) shouldBe true
        }
    }
}