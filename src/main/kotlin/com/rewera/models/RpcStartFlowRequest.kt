package com.rewera.models

data class RpcStartFlowRequest(val clientId: String, val flowName: String, val parameters: RpcStartFlowRequestParameters)
