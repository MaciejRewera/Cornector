package com.rewera.controllers

import com.rewera.connectors.CordaNodeConnector
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.*

class FlowManagerControllerSpec {

    private val cordaNodeConnector = Mockito.mock(CordaNodeConnector::class.java)

    private val flowManagerController = FlowManagerController(cordaNodeConnector)

    @BeforeEach
    fun setup() {
        reset(cordaNodeConnector)
    }

    @Nested
    @DisplayName("FlowManagerController on killFlow")
    inner class KillFlowSpec {

        private val flowId = UUID.randomUUID()

        @Test
        fun `should call CordaNodeConnector`() {
            whenever(cordaNodeConnector.killFlow(any())).thenReturn(true)

            flowManagerController.killFlow(flowId.toString())

            verify(cordaNodeConnector).killFlow(eq(flowId))
        }

        @Test
        fun `should return value returned by CordaNodeConnector`() {
            whenever(cordaNodeConnector.killFlow(any())).thenReturn(true)

            flowManagerController.killFlow(flowId.toString()) shouldBe true
        }
    }
}