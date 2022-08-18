package com.rewera.controllers

import com.rewera.connectors.CordaNodeConnector
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

class FlowStarterControllerSpec : WordSpec({

    val cordaNodeConnector = mock(CordaNodeConnector::class.java)

    val flowStarterController = FlowStarterController(cordaNodeConnector)

    beforeTest {
        reset(cordaNodeConnector)

    }

    "FlowStarterController on getRegisteredFlows" should {

        "call CordaNodeConnector" {
            whenever(cordaNodeConnector.getRegisteredFlows()).thenReturn(emptyList())

            flowStarterController.getRegisteredFlows()

            verify(cordaNodeConnector).getRegisteredFlows()
        }

        "return value returned by CordaNodeConnector" {
            val flows = listOf("test.flow.1", "test.flow.2", "test.flow.3")
            whenever(cordaNodeConnector.getRegisteredFlows()).thenReturn(flows)

            flowStarterController.getRegisteredFlows() shouldBe flows
        }
    }


})