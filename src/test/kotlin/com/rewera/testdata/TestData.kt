package com.rewera.testdata

import net.corda.core.concurrent.CordaFuture
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StateMachineRunId
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.messaging.FlowHandleWithClientId
import java.util.*

object TestData {

    const val clientId = "test-client-id"

    data class FlowResult(val value1: String, val value2: Int)

    fun flowHandleWithClientId(clientId: String, returnValue: FlowResult) = object : FlowHandleWithClientId<FlowResult> {
        override val clientId: String = clientId
        override val returnValue: CordaFuture<FlowResult> = doneFuture(returnValue)
        override val id: StateMachineRunId = StateMachineRunId(UUID.randomUUID())
        override fun close() {}
    }

    class SingleParameterTestFlow(someParameter: String) : FlowLogic<String>() {
        override fun call(): String = "SingleParameterTestFlow result should be here."
    }
}