package com.rewera.routing

import com.google.inject.AbstractModule
import com.rewera.connectors.CordaRpcOpsFactory
import org.mockito.Mockito.mock

class ConnectorsMockModule : AbstractModule() {
    override fun configure() = bind(CordaRpcOpsFactory::class.java).toInstance(mock(CordaRpcOpsFactory::class.java))
}