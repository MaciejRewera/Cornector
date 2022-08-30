package com.rewera.models.api

data class RpcStartFlowRequest(val clientId: String, val flowName: String, val parameters: RpcStartFlowRequestParameters)
