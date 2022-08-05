package com.rewera.connectors

import com.google.inject.Inject
import com.google.inject.Singleton
import io.ktor.events.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.client.rpc.RPCConnection
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort

@Singleton
class CornectorRpcOps @Inject constructor(
    private val config: ApplicationConfig,
    applicationLifecycleEvents: Events
) {
    private val rpcConnection: RPCConnection<CordaRPCOps> by lazy {
        val connection = createCordaRpcConnection()

        applicationLifecycleEvents.subscribe(ApplicationStopped) {
            rpcConnection.notifyServerAndClose()
        }

        connection
    }
    val rpcOps: CordaRPCOps by lazy { rpcConnection.proxy }

    private fun createCordaRpcConnection(): CordaRPCConnection {
        val host = config.property("corda.node.address.rpc.host").getString()
        val port = config.property("corda.node.address.rpc.port").getString().toInt()
        val nodeRpcAddress = NetworkHostAndPort(host, port)
        val rpcUserName = config.property("corda.node.username").getString()
        val rpcUserPassword = config.property("corda.node.password").getString()

        val gracefulReconnect = GracefulReconnect(onDisconnect = {}, onReconnect = {}, maxAttempts = -1)
        val cordaClient = CordaRPCClient(nodeRpcAddress)

        println("Starting RPC connection to $host:$port with username:[$rpcUserName], password:[$rpcUserPassword]")
        return cordaClient.start(rpcUserName, rpcUserPassword, gracefulReconnect = gracefulReconnect)
    }
}
