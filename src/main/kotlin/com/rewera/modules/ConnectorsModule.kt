package com.rewera.modules

import com.google.inject.AbstractModule
import com.rewera.connectors.CornectorRpcOps

class ConnectorsModule : AbstractModule() {
    override fun configure() = bind(CornectorRpcOps::class.java).asEagerSingleton()
}
