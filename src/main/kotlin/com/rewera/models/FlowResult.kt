package com.rewera.models

import com.rewera.models.api.FlowStatus

data class FlowResult<A>(
    val clientId: String,
    val flowId: String? = null,
    val result: A? = null,
    val status: FlowStatus = FlowStatus.RUNNING
)