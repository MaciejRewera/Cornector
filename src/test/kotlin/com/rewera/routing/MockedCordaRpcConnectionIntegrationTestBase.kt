package com.rewera.routing

import com.google.inject.Guice
import com.google.inject.Injector
import com.rewera.controllers.ControllersRegistry
import com.rewera.controllers.FlowManagerController
import com.rewera.controllers.FlowStarterController
import com.rewera.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.mockito.kotlin.reset

abstract class MockedCordaRpcConnectionIntegrationTestBase {

    protected val flowStarterController: FlowStarterController = Mockito.mock(FlowStarterController::class.java)
    protected val flowManagerController: FlowManagerController = Mockito.mock(FlowManagerController::class.java)

    private val testInjector: Injector = Guice.createInjector()

    @BeforeEach
    fun resetControllers() {
        reset(flowStarterController, flowManagerController)
    }

    @BeforeAll
    @Suppress("unused")
    fun setupTestServer() {
        testInjector.injectMembers(this)
    }

    fun testApplicationRoutesOnly(
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
        configureRouting(ControllersRegistry(flowStarterController, flowManagerController))
    }
}
