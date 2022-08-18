package com.rewera.connectors

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.future.await
import net.corda.core.concurrent.CordaFuture
import net.corda.core.flows.StateMachineRunId
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowHandleWithClientId
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.*
import java.util.concurrent.TimeUnit


class CordaNodeConnectorSpec : WordSpec({

    val cordaRpcOpsFactory = Mockito.mock(CordaRpcOpsFactory::class.java)
    val rpcOps = Mockito.mock(CordaRPCOps::class.java)

    val cordaNodeConnector = CordaNodeConnector(cordaRpcOpsFactory)

    beforeTest {
        reset(cordaRpcOpsFactory, rpcOps)
        whenever(cordaRpcOpsFactory.rpcOps).thenReturn(rpcOps)
    }

    "CordaNodeConnector on getRegisteredFlows" should {

        "call CordaRPCOps" {
            whenever(rpcOps.registeredFlows()).thenReturn(emptyList())

            cordaNodeConnector.getRegisteredFlows()

            verify(rpcOps).registeredFlows()
        }

        "return empty list when CordaRPCOps returns empty list" {
            whenever(rpcOps.registeredFlows()).thenReturn(emptyList())

            cordaNodeConnector.getRegisteredFlows() shouldBe emptyList()
        }

        "return the value from CordaRPCOps when it returns non-empty list" {
            val flows = listOf("test.flow.name")
            whenever(rpcOps.registeredFlows()).thenReturn(flows)

            cordaNodeConnector.getRegisteredFlows() shouldBe flows
        }
    }

    "CordaNodeConnector on getFlowOutcomeForClientId" should {

        data class FlowResult(val value1: String, val value2: Int)

        val testReturnValue = FlowResult("Test value", 1234567)

        fun flowHandleWithClientId(clientId: String) = object : FlowHandleWithClientId<FlowResult> {
            override val clientId: String = clientId
            override val returnValue: CordaFuture<FlowResult> = doneFuture(testReturnValue)
            override val id: StateMachineRunId = StateMachineRunId(UUID.randomUUID())
            override fun close() {}
        }

        "call CordaRPCOps" {
            val clientId = "test-client-id"
            whenever(rpcOps.reattachFlowWithClientId<FlowResult>(any())).thenReturn(flowHandleWithClientId(clientId))

            cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(clientId)!!.await()

            verify(rpcOps).reattachFlowWithClientId<FlowResult>(eq(clientId))
        }

        "return Future with value returned by CordaRPCOps" {
            val clientId = "test-client-id"
            whenever(rpcOps.reattachFlowWithClientId<FlowResult>(any())).thenReturn(flowHandleWithClientId(clientId))

            val result = cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(clientId)!!.await()

            result shouldBe testReturnValue
        }

    }

})