package com.rewera.routing

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.rewera.connectors.CordaRpcOpsFactory
import com.rewera.instance
import com.rewera.modules.MainModule
import com.rewera.plugins.configureRouting
import com.rewera.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.reset

abstract class MockedCordaRpcConnectionIntegrationTestBase {

    private val engine: TestApplicationEngine = TestEngine.create(createTestEnvironment()) {}
    private val testInjector: Injector = Guice.createInjector(MainModule(engine.application), ConnectorsMockModule())

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

    @AfterAll
    @Suppress("unused")
    fun stopServer() {
        engine.stop(0L, 0L)
    }

    fun ApplicationTestBuilder.setupTestModules() {
        environment {
            config = MapApplicationConfig("ktor.application.modules.size" to "0")
            module { testModule() }
        }
    }

    private fun Application.testModule() {
        configureSerialization()
        configureRouting(testInjector.instance())
    }
}
