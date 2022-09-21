package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.models.api.FlowId
import com.rewera.models.api.RpcStartFlowRequestParameters
import com.rewera.models.api.RpcStartFlowResponse
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StateMachineRunId
import java.util.*
import java.util.concurrent.CompletableFuture

@Singleton
class CordaNodeConnector @Inject constructor(
    private val cordaRpcOpsFactory: CordaRpcOpsFactory,
    private val parametersExtractor: FlowClassConstructorParametersExtractor,
    private val flowClassBuilder: FlowClassBuilder
) {

    fun getRegisteredFlows(): List<String> = cordaRpcOpsFactory.rpcOps.registeredFlows()

    fun <T> getFlowOutcomeForClientId(clientId: String): CompletableFuture<T>? =
        cordaRpcOpsFactory.rpcOps.reattachFlowWithClientId<T>(clientId)?.returnValue?.toCompletableFuture()

    fun startFlowTyped(
        clientId: String,
        flowName: String,
        flowParameters: RpcStartFlowRequestParameters
    ): RpcStartFlowResponse {
        val flowClass: Class<out FlowLogic<*>> = flowClassBuilder.buildFlowClass(flowName)
        val flowParametersList = parametersExtractor.extractParameters(flowClass, flowParameters)

        val flowHandle =
            cordaRpcOpsFactory.rpcOps.startFlowDynamicWithClientId(
                clientId,
                flowClass,
                *flowParametersList.toTypedArray()
            )

        return RpcStartFlowResponse(flowHandle.clientId, FlowId(flowHandle.id.uuid))
    }

    fun killFlow(flowId: UUID): Boolean = cordaRpcOpsFactory.rpcOps.killFlow(StateMachineRunId(flowId))
}
