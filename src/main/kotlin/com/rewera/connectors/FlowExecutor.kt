package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.models.api.FlowId
import com.rewera.models.api.FlowStatus
import com.rewera.models.api.RpcStartFlowRequestParameters
import com.rewera.models.api.RpcStartFlowResponse
import com.rewera.repositories.FlowResultRepository
import net.corda.core.flows.FlowLogic
import net.corda.core.messaging.CordaRPCOps
import java.util.*

@Singleton
class FlowExecutor @Inject constructor(
    private val cordaRpcOps: CordaRPCOps,
    private val flowResultRepository: FlowResultRepository,
    private val flowClassBuilder: FlowClassBuilder
) {

    fun startFlow(
        clientId: String,
        flowName: String,
        flowParameters: RpcStartFlowRequestParameters
    ): RpcStartFlowResponse {
        val flowClass: Class<out FlowLogic<*>> = flowClassBuilder.buildFlowClass(flowName)

        flowResultRepository.insertWithClientId(clientId)
        val flowHandle =
            cordaRpcOps.startFlowDynamicWithClientId(clientId, flowClass, flowParameters.parametersInJson)

        val flowId = flowHandle.id.uuid
        flowHandle.returnValue.toCompletableFuture().thenApply { doOnResult(it, clientId, flowId) }

        flowResultRepository.updateFlowId(clientId, flowId)

        return RpcStartFlowResponse(flowHandle.clientId, FlowId(flowId))
    }

//    private fun <A> reattachToFlow(clientId: String) =
//        cordaRpcOpsFactory.rpcOps.reattachFlowWithClientId<A>(clientId).let { flowHandle ->
//            flowHandle.returnValue.toCompletableFuture().thenApply {
//                doOnResult(it, clientId, flowHandle.id.uuid)
//            }
//        }

    private fun <A> doOnResult(flowResult: A, clientId: String, flowId: UUID) {
        flowResultRepository.update(clientId, flowId, FlowStatus.COMPLETED, flowResult)
        cordaRpcOps.removeClientId(clientId)
    }


}