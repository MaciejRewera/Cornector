package com.rewera.models.api

data class RpcFlowOutcomeResponse(
    val exceptionDigest: ExceptionDigest? = null,
    val resultJson: String? = null,
    val status: FlowStatus
)
