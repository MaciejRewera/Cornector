package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class CordaNodeConnector @Inject constructor(private val cornectorRpcOps: CornectorRpcOps) {

    fun getRegisteredFlows(): List<String> = cornectorRpcOps.rpcOps.registeredFlows()
}