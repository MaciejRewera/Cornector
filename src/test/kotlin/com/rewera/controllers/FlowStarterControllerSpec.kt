package com.rewera.controllers

import com.rewera.connectors.CordaNodeConnector
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.*

class FlowStarterControllerSpec : WordSpec({

    val cordaNodeConnector = mock(CordaNodeConnector::class.java)

    val flowStarterController = FlowStarterController(cordaNodeConnector)

    beforeTest {
        reset(cordaNodeConnector)
        `when`(cordaNodeConnector.getRegisteredFlows()).thenReturn(emptyList())
    }

    "FlowStarterController on getRegisteredFlows" should {

        "call CordaNodeConnector" {
            flowStarterController.getRegisteredFlows()

            verify(cordaNodeConnector).getRegisteredFlows()
        }

        "return value returned by CordaNodeConnector" {
            val flows = listOf("test.flow.1", "test.flow.2", "test.flow.3")
            `when`(cordaNodeConnector.getRegisteredFlows()).thenReturn(flows)

            flowStarterController.getRegisteredFlows() shouldBe flows
        }
    }


})