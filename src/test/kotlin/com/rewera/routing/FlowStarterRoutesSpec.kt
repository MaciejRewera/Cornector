package com.rewera.routing

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.corda.core.messaging.CordaRPCOps
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowStarterRoutesSpec : MockedCordaRpcConnectionIntegrationTestBase() {

    private val rpcOps = Mockito.mock(CordaRPCOps::class.java)

    @BeforeEach
    fun setup() {
        reset(rpcOps)
    }

    @Test
    fun `registeredflows endpoint should return the value from CordaRPCOps`() = testApplication {
        setupTestModules()

        whenever(cornectorRpcOps.rpcOps).thenReturn(rpcOps)
        whenever(rpcOps.registeredFlows()).thenReturn(listOf("com.test.FlowName.1", "com.test.FlowName.2"))

        val response = client.get("/api/v1/flowstarter/registeredflows")

        response.status shouldBe HttpStatusCode.OK
        response.bodyAsText() shouldBe "[\"com.test.FlowName.1\",\"com.test.FlowName.2\"]"
    }

}