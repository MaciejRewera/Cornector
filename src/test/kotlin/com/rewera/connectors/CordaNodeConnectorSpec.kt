package com.rewera.connectors

import com.rewera.models.RpcStartFlowRequestParameters
import com.rewera.testdata.TestData.FlowResult
import com.rewera.testdata.TestData.clientId
import com.rewera.testdata.TestData.flowHandleWithClientId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.future.await
import net.corda.core.flows.FlowLogic
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
    private val parametersExtractor = Mockito.mock(FlowClassConstructorParametersExtractor::class.java)
    private val rpcOps = Mockito.mock(CordaRPCOps::class.java)

    private val cordaNodeConnector = CordaNodeConnector(cordaRpcOpsFactory, parametersExtractor)

    @BeforeEach
    fun setup() {
        reset(cordaRpcOpsFactory, rpcOps, parametersExtractor)
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

        private val testReturnValue = FlowResult("Test value", 1234567)

        @Test
        suspend fun `should call CordaRPCOps`() {
            whenever(rpcOps.reattachFlowWithClientId<FlowResult>(any()))
                .thenReturn(flowHandleWithClientId(clientId, testReturnValue))

            cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(clientId)!!.await()

            verify(rpcOps).reattachFlowWithClientId<FlowResult>(eq(clientId))
        }

        @Test
        suspend fun `should return Future with value returned by CordaRPCOps`() {
            whenever(rpcOps.reattachFlowWithClientId<FlowResult>(any()))
                .thenReturn(flowHandleWithClientId(clientId, testReturnValue))

            val result = cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(clientId)!!.await()

            result shouldBe testReturnValue
        }
    }

    @Nested
    @DisplayName("CordaNodeConnector on startFlow")
    inner class StartFlowSpec {

        inner class SingleParameterTestFlow(someParameter: String) : FlowLogic<String>() {
            override fun call(): String = "SingleParameterTestFlow result should be here."
        }

        private val flowName = "com.rewera.connectors.CordaNodeConnectorSpec\$StartFlowSpec\$SingleParameterTestFlow"
        private val flowIdValue = UUID.randomUUID()
        private val flowHandle =
            FlowHandleWithClientIdImpl(StateMachineRunId(flowIdValue), doneFuture("Result"), clientId)
        private val flowParams = RpcStartFlowRequestParameters("This should be a JSON")

        @Test
        fun `should call CordaRPCOps`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)

            cordaNodeConnector.startFlow(clientId, flowName, flowParams)

            verify(rpcOps).startFlowDynamicWithClientId(
                eq(clientId),
                eq(SingleParameterTestFlow::class.java),
                eq(flowParams.parametersInJson)
            )
        }

        @Test
        fun `should return RpcStartFlowResponse with flowId returned from CordaRPCOps`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)

            val result = cordaNodeConnector.startFlow(clientId, flowName, flowParams)

            result.clientId shouldBe clientId
            result.flowId.uuid shouldBe flowIdValue
        }

        @Test
        fun `when provided with incorrect class name should throw an exception`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)

            val exc = shouldThrow<ClassNotFoundException> {
                cordaNodeConnector.startFlow(clientId, "this.is.not.a.valid.class.name", flowParams)
            }
            exc.message shouldBe "this.is.not.a.valid.class.name"
        }

        @Test
        fun `when provided with class name that does not inherit FlowLogic should should call CordaRPCOps`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)

            cordaNodeConnector.startFlow(clientId, String::class.java.name, flowParams)

            verify(rpcOps).startFlowDynamicWithClientId<String>(eq(clientId), any(), eq(flowParams.parametersInJson))
        }
    }

    @Nested
    @DisplayName("CordaNodeConnector on startFlowTyped")
    inner class StartFlowTypedSpec {

        inner class SingleParameterTestFlow(someParameter: String) : FlowLogic<String>() {
            override fun call(): String = "SingleParameterTestFlow result should be here."
        }

        inner class MultipleParametersTestFlow(
            firstParameter: String,
            secondParameter: Int,
            thirdParameter: String
        ) : FlowLogic<String>() {
            override fun call(): String = "MultipleParametersTestFlow result should be here."
        }

        private val singleParameterFlowName =
            "com.rewera.connectors.CordaNodeConnectorSpec\$StartFlowTypedSpec\$SingleParameterTestFlow"
        private val multipleParametersFlowName =
            "com.rewera.connectors.CordaNodeConnectorSpec\$StartFlowTypedSpec\$MultipleParametersTestFlow"
        private val flowIdValue = UUID.randomUUID()
        private val someParameterValue = "Test param value"
        private val flowHandle =
            FlowHandleWithClientIdImpl(StateMachineRunId(flowIdValue), doneFuture("Result"), clientId)
        private val flowParams = RpcStartFlowRequestParameters("{\"someParameter\":\"$someParameterValue\"}")

        @Test
        fun `should call CordaRPCOps`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)
            whenever(parametersExtractor.extractParameters<Any>(any(), any())).thenReturn(listOf(someParameterValue))

            cordaNodeConnector.startFlowTyped(clientId, singleParameterFlowName, flowParams)

            verify(rpcOps).startFlowDynamicWithClientId(
                eq(clientId),
                eq(SingleParameterTestFlow::class.java),
                eq(someParameterValue)
            )
        }

        @Test
        fun `should return RpcStartFlowResponse with flowId returned from CordaRPCOps`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)
            whenever(parametersExtractor.extractParameters<Any>(any(), any())).thenReturn(listOf(someParameterValue))

            val result = cordaNodeConnector.startFlowTyped(clientId, singleParameterFlowName, flowParams)

            result.clientId shouldBe clientId
            result.flowId.uuid shouldBe flowIdValue
        }

        @Test
        fun `when provided with incorrect class name should throw an exception`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)

            val exc = shouldThrow<ClassNotFoundException> {
                cordaNodeConnector.startFlowTyped(clientId, "this.is.not.a.valid.class.name", flowParams)
            }
            exc.message shouldBe "this.is.not.a.valid.class.name"
        }

        @Test
        fun `when provided with class name that does not inherit FlowLogic should should call CordaRPCOps`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)
            whenever(parametersExtractor.extractParameters<Any>(any(), any())).thenReturn(listOf(someParameterValue))

            cordaNodeConnector.startFlowTyped(clientId, String::class.java.name, flowParams)

            verify(rpcOps).startFlowDynamicWithClientId<String>(eq(clientId), any(), eq(someParameterValue))
        }

        @Test
        fun `when provided with flow that has multiple constructor params should call CordaRPCOps with params obtained from parametersExtractor`() {
            whenever(rpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)
            val paramExtractorReturnedValues = listOf("Test value 1", 1234567, "Test value 3")
            whenever(parametersExtractor.extractParameters<Any>(any(), any())).thenReturn(paramExtractorReturnedValues)

            val multipleFlowParams = RpcStartFlowRequestParameters(
                "{\"firstParameter\":\"Test value 1\", \"secondParameter\":1234567, \"thirdParameter\":\"Test value 3\"}"
            )

            cordaNodeConnector.startFlowTyped(clientId, multipleParametersFlowName, multipleFlowParams)

            val expectedVararg = paramExtractorReturnedValues.toTypedArray()
            verify(rpcOps).startFlowDynamicWithClientId(
                clientId,
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