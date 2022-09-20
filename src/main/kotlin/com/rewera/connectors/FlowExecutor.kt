package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.models.api.FlowId
import com.rewera.models.api.FlowStatus
import com.rewera.models.api.RpcStartFlowRequestParameters
import com.rewera.models.api.RpcStartFlowResponse
import com.rewera.repositories.FlowResultsRepository
import net.corda.core.flows.FlowLogic
import java.util.*

@Singleton
class FlowExecutor @Inject constructor(
    private val cordaRpcOpsFactory: CordaRpcOpsFactory,
    private val flowResultsRepository: FlowResultsRepository,
    private val flowClassBuilder: FlowClassBuilder
) {

    fun startFlow(
        clientId: String,
        flowName: String,
        flowParameters: RpcStartFlowRequestParameters
    ): RpcStartFlowResponse {
        val flowClass: Class<out FlowLogic<*>> = flowClassBuilder.buildFlowClass(flowName)

        flowResultsRepository.insertWithClientId(clientId)
        val flowHandle =
            cordaRpcOpsFactory.rpcOps.startFlowDynamicWithClientId(clientId, flowClass, flowParameters.parametersInJson)

        val flowId = flowHandle.id.uuid
        flowHandle.returnValue.toCompletableFuture().thenApply { doOnResult(it, clientId, flowId) }

        flowResultsRepository.updateFlowId(clientId, flowId)

        return RpcStartFlowResponse(flowHandle.clientId, FlowId(flowId))
    }

//    private fun <A> reattachToFlow(clientId: String) =
//        cordaRpcOpsFactory.rpcOps.reattachFlowWithClientId<A>(clientId).let { flowHandle ->
//            flowHandle.returnValue.toCompletableFuture().thenApply {
//                doOnResult(it, clientId, flowHandle.id.uuid)
//            }
//        }

    private fun <A> doOnResult(flowResult: A, clientId: String, flowId: UUID) {
//        flowResultsRepository.updateFlowId(clientId, flowId)
        flowResultsRepository.update(clientId, FlowStatus.COMPLETED, flowResult)
        cordaRpcOpsFactory.rpcOps.removeClientId(clientId)
    }


}