package com.rewera.routing

import com.google.inject.AbstractModule
import com.rewera.connectors.CornectorRpcOps
import org.mockito.Mockito.mock

class ConnectorsMockModule : AbstractModule() {
    override fun configure() = bind(CornectorRpcOps::class.java).toInstance(mock(CornectorRpcOps::class.java))
}