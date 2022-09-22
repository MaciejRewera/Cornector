package com.rewera.models.api

import com.rewera.models.FlowResult
import com.rewera.modules.Jackson

data class RpcFlowOutcomeResponse(
    val exceptionDigest: ExceptionDigest? = null,
    val resultJson: String? = null,
    val status: FlowStatus
) {
    companion object {
        fun fromFlowResult(flowResult: FlowResult<*>): RpcFlowOutcomeResponse = when (flowResult.status) {
            FlowStatus.COMPLETED -> RpcFlowOutcomeResponse(
                status = FlowStatus.COMPLETED,
                resultJson = Jackson.mapper.writeValueAsString(flowResult.result)
            )
            FlowStatus.FAILED -> RpcFlowOutcomeResponse(
                status = FlowStatus.FAILED,
                exceptionDigest = flowResult.exceptionDigest
            )
            FlowStatus.RUNNING -> RpcFlowOutcomeResponse(status = FlowStatus.RUNNING)
        }
    }
}
