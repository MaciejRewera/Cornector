package com.rewera.controllers

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.connectors.CordaNodeConnector
import com.rewera.connectors.FlowExecutor
import com.rewera.models.api.RpcFlowOutcomeResponse
import com.rewera.models.api.RpcStartFlowRequest
import com.rewera.models.api.RpcStartFlowResponse
import com.rewera.repositories.FlowResultRepository
import io.ktor.server.plugins.*
import java.util.*

@Singleton
class FlowStarterController @Inject constructor(
    private val cordaNodeConnector: CordaNodeConnector,
    private val flowExecutor: FlowExecutor,
    private val flowResultRepository: FlowResultRepository
) {

    fun getRegisteredFlows(): List<String> = cordaNodeConnector.getRegisteredFlows()

    fun getFlowOutcomeForClientId(clientId: String): RpcFlowOutcomeResponse =
        flowResultRepository.findByClientId(clientId)?.toRpcFlowOutcomeResponse() ?: throw NotFoundException()

    fun getFlowOutcomeForFlowId(flowId: UUID): RpcFlowOutcomeResponse =
        flowResultRepository.findByFlowId(flowId)?.toRpcFlowOutcomeResponse() ?: throw NotFoundException()

    fun startFlow(rpcStartFlowRequest: RpcStartFlowRequest): RpcStartFlowResponse =
        flowExecutor.startFlow(
            clientId = rpcStartFlowRequest.clientId,
            flowName = rpcStartFlowRequest.flowName,
            flowParameters = rpcStartFlowRequest.parameters
        )
}
