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
import net.corda.core.messaging.FlowHandle

@Singleton
class FlowExecutor @Inject constructor(
    private val cordaRpcOps: CordaRPCOps,
    private val flowResultRepository: FlowResultRepository,
    private val flowClassBuilder: FlowClassBuilder
) {

    init {
        try {
            reattachToRunningFlows()
        } catch (e: Throwable) {
            // TODO: Log exception once logging is set up
        }
    }

    private fun reattachToRunningFlows() =
        flowResultRepository.findByStatus(FlowStatus.RUNNING).forEach { runningFlow ->
            cordaRpcOps.reattachFlowWithClientId<Any?>(runningFlow.clientId)
                ?.let { handleResult(runningFlow.clientId, it) }
        }

    fun startFlow(
        clientId: String,
        flowName: String,
        flowParameters: RpcStartFlowRequestParameters
    ): RpcStartFlowResponse {
        val flowClass: Class<out FlowLogic<*>> = flowClassBuilder.buildFlowClass(flowName)

        flowResultRepository.insertWithClientId(clientId)
        val flowHandle =
            cordaRpcOps.startFlowDynamicWithClientId(clientId, flowClass, flowParameters.parametersInJson)

        handleResult(clientId, flowHandle)

        val flowId = flowHandle.id.uuid
        flowResultRepository.updateFlowId(clientId, flowId)

        return RpcStartFlowResponse(flowHandle.clientId, FlowId(flowId))
    }

    private fun <A> handleResult(clientId: String, flowHandle: FlowHandle<A>) =
        flowHandle.returnValue.toCompletableFuture().thenApply {
            flowResultRepository.update(clientId, flowHandle.id.uuid, FlowStatus.COMPLETED, it)
            cordaRpcOps.removeClientId(clientId)
        }

}
