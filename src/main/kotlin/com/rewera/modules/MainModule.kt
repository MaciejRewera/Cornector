package com.rewera.modules

import com.google.inject.AbstractModule
import com.rewera.DynamicClassLoader
import io.ktor.events.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import net.corda.core.messaging.CordaRPCOps

class MainModule(private val application: Application) : AbstractModule() {
    override fun configure() {
        bind(Application::class.java).toInstance(application)
        bind(ApplicationConfig::class.java).toInstance(application.environment.config)
        bind(Events::class.java).toInstance(application.environment.monitor)
        bind(DynamicClassLoader::class.java).toProvider(DynamicClassLoaderProvider::class.java)
        bind(CordaRPCOps::class.java).toProvider(LazyCordaRpcOpsProvider::class.java)
    }
}
