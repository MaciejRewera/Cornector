package com.rewera.routing

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.rewera.connectors.CordaRpcOpsFactory
import com.rewera.instance
import com.rewera.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.reset

abstract class MockedCordaRpcConnectionIntegrationTestBase {

    private val testInjector: Injector = Guice.createInjector(ConnectorsMockModule())

    @Inject
    protected lateinit var cordaRpcOpsFactory: CordaRpcOpsFactory

    @BeforeEach
    fun resetCornectorRpcOps() {
        reset(cordaRpcOpsFactory)
    }

    @BeforeAll
    @Suppress("unused")
    fun setupTestServer() {
        testInjector.injectMembers(this)
    }

    fun testApplicationWithMockedRpcConnection(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
        setupTestModules()
        block()
    }


    private fun ApplicationTestBuilder.setupTestModules() {
        environment {
            config = MapApplicationConfig("ktor.application.modules.size" to "0")
            module { testModule() }
        }
    }

    private fun Application.testModule() {
        configureRouting(testInjector.instance())
    }
}
