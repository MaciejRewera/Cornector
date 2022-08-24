package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.models.FlowId
import com.rewera.models.RpcStartFlowRequestParameters
import com.rewera.models.RpcStartFlowResponse
import net.corda.core.flows.FlowLogic
import java.util.concurrent.CompletableFuture

@Singleton
class CordaNodeConnector @Inject constructor(
    private val cordaRpcOpsFactory: CordaRpcOpsFactory,
    private val parametersExtractor: FlowClassConstructorParametersExtractor
) {

    fun getRegisteredFlows(): List<String> = cordaRpcOpsFactory.rpcOps.registeredFlows()

    fun <T> getFlowOutcomeForClientId(clientId: String): CompletableFuture<T>? =
        cordaRpcOpsFactory.rpcOps.reattachFlowWithClientId<T>(clientId)?.returnValue?.toCompletableFuture()

    // TODO: Would be better if this method ensured the flowClass is for a class inheriting from FlowLogic.
    fun startFlow(
        clientId: String,
        flowName: String,
        flowParameters: RpcStartFlowRequestParameters
    ): RpcStartFlowResponse {
        val flowClass: Class<out FlowLogic<*>> = Class.forName(flowName) as Class<out FlowLogic<*>>
        val flowHandle =
            cordaRpcOpsFactory.rpcOps.startFlowDynamicWithClientId(clientId, flowClass, flowParameters.parametersInJson)

        return RpcStartFlowResponse(flowHandle.clientId, FlowId(flowHandle.id.uuid))
    }

    fun startFlowTyped(
        clientId: String,
        flowName: String,
        flowParameters: RpcStartFlowRequestParameters
    ): RpcStartFlowResponse {
        val flowClass: Class<out FlowLogic<*>> = Class.forName(flowName) as Class<out FlowLogic<*>>
        val flowParametersList = parametersExtractor.extractParameters(flowClass, flowParameters)

        val flowHandle =
            cordaRpcOpsFactory.rpcOps.startFlowDynamicWithClientId(
                clientId,
                flowClass,
                *flowParametersList.toTypedArray()
            )

        return RpcStartFlowResponse(flowHandle.clientId, FlowId(flowHandle.id.uuid))
    }
}
