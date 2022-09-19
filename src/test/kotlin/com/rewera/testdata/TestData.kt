package com.rewera.testdata

import net.corda.core.concurrent.CordaFuture
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StateMachineRunId
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.messaging.FlowHandleWithClientId
import java.util.*

object TestData {

    const val testClientId = "test-client-id"

    fun randomUuid(): UUID = UUID.randomUUID()
    fun randomUuidString(): String = randomUuid().toString()

    data class TestFlowResult(val value1: String, val value2: Int)

    fun flowHandleWithClientId(clientId: String, returnValue: TestFlowResult) =
        object : FlowHandleWithClientId<TestFlowResult> {
            override val clientId: String = clientId
            override val returnValue: CordaFuture<TestFlowResult> = doneFuture(returnValue)
            override val id: StateMachineRunId = StateMachineRunId(UUID.randomUUID())
            override fun close() {}
        }

    class ParameterlessTestFlow : FlowLogic<String>() {
        override fun call(): String = "ParameterlessTestFlow result should be here."
    }

    class SingleParameterTestFlow(someParameter: String) : FlowLogic<String>() {
        override fun call(): String = "SingleParameterTestFlow result should be here."
    }

    class MultipleParametersTestFlow(
        firstParameter: String,
        secondParameter: Int,
        thirdParameter: String
    ) : FlowLogic<String>() {
        override fun call(): String = "MultipleParametersTestFlow result should be here."
    }

}