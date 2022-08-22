package com.rewera.controllers

import com.rewera.connectors.CordaNodeConnector
import com.rewera.models.ExceptionDigest
import com.rewera.models.FlowStatus
import com.rewera.models.RpcFlowOutcomeResponse
import com.rewera.testdata.TestData.FlowResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.*
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.internal.concurrent.openFuture
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.util.concurrent.CompletionException


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

    "FlowStarterController on getFlowOutcomeForClientId" should {

        val clientId = "test-client-id"
        val testReturnValue = FlowResult("Test value", 1234567)

        "call CordaNodeConnector" {
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(any()))
                .thenReturn(doneFuture(testReturnValue).toCompletableFuture())

            flowStarterController.getFlowOutcomeForClientId(clientId)

            verify(cordaNodeConnector).getFlowOutcomeForClientId<FlowResult>(eq(clientId))
        }

        "throw NotFoundException when CordaNodeConnector returns null" {
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(any())).thenReturn(null)

            shouldThrow<NotFoundException> { flowStarterController.getFlowOutcomeForClientId(clientId) }
        }

        "return RpcFlowOutcomeResponse with status RUNNING when CordaNodeConnector returns unfinished future" {
            val unfinishedFuture = openFuture<FlowResult>().toCompletableFuture()
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(any())).thenReturn(unfinishedFuture)

            val result = flowStarterController.getFlowOutcomeForClientId(clientId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.RUNNING,
                exceptionDigest = null,
                resultJson = null
            )

            result shouldBe expectedResult
        }

        "return RpcFlowOutcomeResponse with status FAILED when CordaNodeConnector returns exceptionally finished future" {
            val failedFuture = openFuture<FlowResult>().toCompletableFuture()
            val exceptionMessage = "Something went wrong in the flow"
            failedFuture.completeExceptionally(RuntimeException(exceptionMessage))
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(any())).thenReturn(failedFuture)

            val result = flowStarterController.getFlowOutcomeForClientId(clientId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.FAILED,
                exceptionDigest = ExceptionDigest(
                    CompletionException::class.java.name,
                    "${RuntimeException::class.java.name}: $exceptionMessage"
                ),
                resultJson = null
            )

            result shouldBe expectedResult
        }

        "return RpcFlowOutcomeResponse with status COMPLETED when CordaNodeConnector returns finished future" {
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<FlowResult>(any()))
                .thenReturn(doneFuture(testReturnValue).toCompletableFuture())

            val result = flowStarterController.getFlowOutcomeForClientId(clientId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.COMPLETED,
                exceptionDigest = null,
                resultJson = """{"value1":"Test value","value2":1234567}"""
            )

            result shouldBe expectedResult
        }
    }


})
