package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.models.api.FlowId
import com.rewera.models.api.RpcStartFlowRequestParameters
import com.rewera.models.api.RpcStartFlowResponse
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StateMachineRunId
import net.corda.core.messaging.CordaRPCOps
import java.util.*
import java.util.concurrent.CompletableFuture

@Singleton
class CordaNodeConnector @Inject constructor(
    private val cordaRpcOps: CordaRPCOps,
    private val parametersExtractor: FlowClassConstructorParametersExtractor,
    private val flowClassBuilder: FlowClassBuilder
) {

    fun getRegisteredFlows(): List<String> = cordaRpcOps.registeredFlows()

    fun <T> getFlowOutcomeForClientId(clientId: String): CompletableFuture<T>? =
        cordaRpcOps.reattachFlowWithClientId<T>(clientId)?.returnValue?.toCompletableFuture()

    fun startFlowTyped(
        clientId: String,
        flowName: String,
        flowParameters: RpcStartFlowRequestParameters
    ): RpcStartFlowResponse {
        val flowClass: Class<out FlowLogic<*>> = flowClassBuilder.buildFlowClass(flowName)
        val flowParametersList = parametersExtractor.extractParameters(flowClass, flowParameters)

        val flowHandle =
            cordaRpcOps.startFlowDynamicWithClientId(
                clientId,
                flowClass,
                *flowParametersList.toTypedArray()
            )

        return RpcStartFlowResponse(flowHandle.clientId, FlowId(flowHandle.id.uuid))
    }

    fun killFlow(flowId: UUID): Boolean = cordaRpcOps.killFlow(StateMachineRunId(flowId))
}
