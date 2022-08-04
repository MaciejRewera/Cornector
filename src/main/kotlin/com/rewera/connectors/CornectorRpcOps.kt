package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import io.ktor.server.config.*
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort

//TODO: RPC connection should be gracefully closed when server shuts down
@Singleton
class CornectorRpcOps @Inject constructor(private val config: ApplicationConfig) {

    private fun createCordaRpcOps(): CordaRPCOps {
        val host = config.property("corda.node.address.rpc.host").getString()
        val port = config.property("corda.node.address.rpc.port").getString().toInt()
        val nodeRpcAddress = NetworkHostAndPort(host, port)
        val rpcUserName = config.property("corda.node.username").toString()
        val rpcUserPassword = config.property("corda.node.password").toString()

        val gracefulReconnect = GracefulReconnect(onDisconnect = {}, onReconnect = {}, maxAttempts = -1)
        val cordaClient = CordaRPCClient(nodeRpcAddress)
        val cordaRpcOps = cordaClient.start(rpcUserName, rpcUserPassword, gracefulReconnect = gracefulReconnect).proxy

        return cordaRpcOps
    }
}
