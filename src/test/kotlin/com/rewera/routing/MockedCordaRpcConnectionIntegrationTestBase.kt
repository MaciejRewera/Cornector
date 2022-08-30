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
import net.corda.core.messaging.CordaRPCOps
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

abstract class MockedCordaRpcConnectionIntegrationTestBase {

    protected val rpcOps = Mockito.mock(CordaRPCOps::class.java)

    private val testInjector: Injector = Guice.createInjector(ConnectorsMockModule())

    @Inject
    private lateinit var cordaRpcOpsFactory: CordaRpcOpsFactory

    @BeforeEach
    fun resetCornectorRpcOps() {
        reset(cordaRpcOpsFactory, rpcOps)
        whenever(cordaRpcOpsFactory.rpcOps).thenReturn(rpcOps)
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
