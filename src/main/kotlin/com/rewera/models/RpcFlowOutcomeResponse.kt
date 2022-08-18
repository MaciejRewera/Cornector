package com.rewera.models

data class RpcFlowOutcomeResponse(
    val exceptionDigest: ExceptionDigest? = null,
    val resultJson: String? = null,
    val status: FlowStatus
)
