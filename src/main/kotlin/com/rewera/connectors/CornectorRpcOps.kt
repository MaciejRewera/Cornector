package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import io.ktor.events.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.utilities.NetworkHostAndPort

@Singleton
class CornectorRpcOps @Inject constructor(
    private val config: ApplicationConfig,
    applicationLifecycleEvents: Events
) {
    private val rpcConnection = createCordaRpcConnection()
    private val rpcOps = rpcConnection.proxy

    init {
        applicationLifecycleEvents.subscribe(ApplicationStopped) {
            rpcConnection.notifyServerAndClose()
        }
    }

    private fun createCordaRpcConnection(): CordaRPCConnection {
        val host = config.property("corda.node.address.rpc.host").getString()
        val port = config.property("corda.node.address.rpc.port").getString().toInt()
        val nodeRpcAddress = NetworkHostAndPort(host, port)
        val rpcUserName = config.property("corda.node.username").toString()
        val rpcUserPassword = config.property("corda.node.password").toString()

        val gracefulReconnect = GracefulReconnect(onDisconnect = {}, onReconnect = {}, maxAttempts = -1)
        val cordaClient = CordaRPCClient(nodeRpcAddress)

        return cordaClient.start(rpcUserName, rpcUserPassword, gracefulReconnect = gracefulReconnect)
    }
}
