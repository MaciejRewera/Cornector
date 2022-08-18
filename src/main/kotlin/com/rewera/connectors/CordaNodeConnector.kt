package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import java.util.concurrent.CompletableFuture

@Singleton
class CordaNodeConnector @Inject constructor(private val cordaRpcOpsFactory: CordaRpcOpsFactory) {

    fun getRegisteredFlows(): List<String> = cordaRpcOpsFactory.rpcOps.registeredFlows()

    fun <T> getFlowOutcomeForClientId(clientId: String): CompletableFuture<T>? =
        cordaRpcOpsFactory.rpcOps.reattachFlowWithClientId<T>(clientId)?.returnValue?.toCompletableFuture()

}
