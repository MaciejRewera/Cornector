package com.rewera.connectors

import net.corda.core.messaging.CordaRPCOps
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import kotlin.test.assertEquals


class CordaNodeConnectorSpec {

    private val cornectorRpcOps = Mockito.mock(CornectorRpcOps::class.java)
    private val rpcOps = Mockito.mock(CordaRPCOps::class.java)

    private val cordaNodeConnector = CordaNodeConnector(cornectorRpcOps)

    @BeforeEach
    fun setup() {
        Mockito.reset(cornectorRpcOps, rpcOps)
        `when`(cornectorRpcOps.rpcOps).thenReturn(rpcOps)
    }

    @Nested
    @DisplayName("CordaNodeConnector on getRegisteredFlows")
    inner class VaultQuerySpec {

        @Test
        fun `should call CordaRPCOps`() {
            `when`(rpcOps.registeredFlows()).thenReturn(emptyList())

            cordaNodeConnector.getRegisteredFlows()

            verify(rpcOps).registeredFlows()
        }

        @Test
        fun `should return empty list when CordaRPCOps returns empty list`() {
            `when`(rpcOps.registeredFlows()).thenReturn(emptyList())

            assertEquals(emptyList(), cordaNodeConnector.getRegisteredFlows())
        }

        @Test
        fun `should return the value from CordaRPCOps when it returns non-empty list`() {
            val flows = listOf("test.flow.name")
            `when`(rpcOps.registeredFlows()).thenReturn(flows)

            assertEquals(flows, cordaNodeConnector.getRegisteredFlows())
        }
    }

}