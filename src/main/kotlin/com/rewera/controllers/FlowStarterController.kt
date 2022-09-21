package com.rewera.controllers

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.connectors.CordaNodeConnector
import com.rewera.connectors.FlowExecutor
import com.rewera.models.api.*
import com.rewera.modules.Jackson
import io.ktor.server.plugins.*

@Singleton
class FlowStarterController @Inject constructor(
    private val cordaNodeConnector: CordaNodeConnector,
    private val flowExecutor: FlowExecutor
) {

    fun getRegisteredFlows(): List<String> = cordaNodeConnector.getRegisteredFlows()

    fun getFlowOutcomeForClientId(clientId: String): RpcFlowOutcomeResponse =
        cordaNodeConnector.getFlowOutcomeForClientId<Any>(clientId)
            ?.thenApply {
                RpcFlowOutcomeResponse(
                    status = FlowStatus.COMPLETED,
                    resultJson = Jackson.mapper.writeValueAsString(it)
                )
            }?.exceptionally {
                RpcFlowOutcomeResponse(
                    status = FlowStatus.FAILED,
                    exceptionDigest = ExceptionDigest(it::class.java.name, it.message)
                )
            }?.getNow(RpcFlowOutcomeResponse(status = FlowStatus.RUNNING))
            ?: throw NotFoundException()

    fun startFlow(rpcStartFlowRequest: RpcStartFlowRequest): RpcStartFlowResponse =
        flowExecutor.startFlow(
            clientId = rpcStartFlowRequest.clientId,
            flowName = rpcStartFlowRequest.flowName,
            flowParameters = rpcStartFlowRequest.parameters
        )
}
