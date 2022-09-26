package com.rewera.modules

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Singleton
import com.rewera.connectors.LazyCordaRpcOps
import io.ktor.events.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.utilities.NetworkHostAndPort

@Singleton
class LazyCordaRpcOpsProvider @Inject constructor(
    config: ApplicationConfig,
    private val applicationLifecycleEvents: Events
) : Provider<LazyCordaRpcOps> {

    override fun get(): LazyCordaRpcOps = LazyCordaRpcOps { createCordaRpcConnection() }

    private val host = config.property("corda.node.address.rpc.host").getString()
    private val port = config.property("corda.node.address.rpc.port").getString().toInt()
    private val nodeRpcAddress = NetworkHostAndPort(host, port)
    private val rpcUserName = config.property("corda.node.username").getString()
    private val rpcUserPassword = config.property("corda.node.password").getString()

    private fun createCordaRpcConnection(): CordaRPCConnection {
        val gracefulReconnect = GracefulReconnect(onDisconnect = {}, onReconnect = {}, maxAttempts = -1)
        val cordaClient = CordaRPCClient(nodeRpcAddress)

        val connection = cordaClient.start(rpcUserName, rpcUserPassword, gracefulReconnect = gracefulReconnect)

        applicationLifecycleEvents.subscribe(ApplicationStopped) {
            connection.notifyServerAndClose()
        }

        return connection
    }
}
