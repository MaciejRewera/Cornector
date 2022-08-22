package com.rewera.connectors

import com.rewera.testdata.TestData.FlowResult
import com.rewera.testdata.TestData.flowHandleWithClientId
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.future.await
import net.corda.core.messaging.CordaRPCOps
import org.mockito.Mockito
import org.mockito.kotlin.*


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

        val clientId = "test-client-id"
        val testReturnValue = FlowResult("Test value", 1234567)

        "call CordaRPCOps" {
            whenever(rpcOps.reattachFlowWithClientId<FlowResult>(any())).thenReturn(
                flowHandleWithClientId(clientId, testReturnValue)
            )

            cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(clientId)!!.await()

            verify(rpcOps).reattachFlowWithClientId<FlowResult>(eq(clientId))
        }

        "return Future with value returned by CordaRPCOps" {
            whenever(rpcOps.reattachFlowWithClientId<FlowResult>(any())).thenReturn(
                flowHandleWithClientId(clientId, testReturnValue)
            )

            val result = cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(clientId)!!.await()

            result shouldBe testReturnValue
        }

    }

})