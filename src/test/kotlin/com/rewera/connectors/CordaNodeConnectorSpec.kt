package com.rewera.connectors

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import net.corda.core.messaging.CordaRPCOps
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify


class CordaNodeConnectorSpec : WordSpec ({

    val cornectorRpcOps = Mockito.mock(CornectorRpcOps::class.java)
    val rpcOps = Mockito.mock(CordaRPCOps::class.java)

    val cordaNodeConnector = CordaNodeConnector(cornectorRpcOps)

    beforeTest {
        Mockito.reset(cornectorRpcOps, rpcOps)
        `when`(cornectorRpcOps.rpcOps).thenReturn(rpcOps)
    }

    "CordaNodeConnector on getRegisteredFlows" should {

        "call CordaRPCOps" {
            `when`(rpcOps.registeredFlows()).thenReturn(emptyList())

            cordaNodeConnector.getRegisteredFlows()

            verify(rpcOps).registeredFlows()
        }

        "return empty list when CordaRPCOps returns empty list" {
            `when`(rpcOps.registeredFlows()).thenReturn(emptyList())

            cordaNodeConnector.getRegisteredFlows() shouldBe emptyList()
        }

        "return the value from CordaRPCOps when it returns non-empty list" {
            val flows = listOf("test.flow.name")
            `when`(rpcOps.registeredFlows()).thenReturn(flows)

            cordaNodeConnector.getRegisteredFlows() shouldBe flows
        }
    }

})