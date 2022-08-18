package com.rewera.controllers

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.connectors.CordaNodeConnector
import com.rewera.models.ExceptionDigest
import com.rewera.models.FlowStatus
import com.rewera.models.RpcFlowOutcomeResponse
import com.rewera.modules.JacksonBuilder
import io.ktor.server.plugins.*

@Singleton
class FlowStarterController @Inject constructor(private val cordaNodeConnector: CordaNodeConnector) {

    fun getRegisteredFlows(): List<String> = cordaNodeConnector.getRegisteredFlows()

    fun getFlowOutcomeForClientId(clientId: String): RpcFlowOutcomeResponse =
        cordaNodeConnector.getFlowOutcomeForClientId<Any>(clientId)
            ?.thenApply {
                RpcFlowOutcomeResponse(
                    status = FlowStatus.COMPLETED,
                    resultJson = JacksonBuilder.jackson.writeValueAsString(it)
                )
            }?.exceptionally {
                RpcFlowOutcomeResponse(
                    status = FlowStatus.FAILED,
                    exceptionDigest = ExceptionDigest(it::class.java.name, it.message)
                )
            }?.getNow(RpcFlowOutcomeResponse(status = FlowStatus.RUNNING))
            ?: throw NotFoundException()

}
