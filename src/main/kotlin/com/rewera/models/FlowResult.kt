package com.rewera.models

import com.rewera.models.api.ExceptionDigest
import com.rewera.models.api.FlowStatus
import com.rewera.models.api.RpcFlowOutcomeResponse

data class FlowResult<A>(
    val clientId: String,
    val flowId: String? = null,
    val result: A? = null,
    val exceptionDigest: ExceptionDigest? = null,
    val status: FlowStatus = FlowStatus.RUNNING
) {
    fun toRpcFlowOutcomeResponse() = RpcFlowOutcomeResponse.fromFlowResult(this)
}