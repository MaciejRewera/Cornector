package com.rewera.connectors

import com.fasterxml.jackson.core.JsonParseException
import com.rewera.models.RpcStartFlowRequestParameters
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import net.corda.core.flows.FlowLogic
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FlowClassConstructorParametersExtractorSpec {

    private val extractor = FlowClassConstructorParametersExtractor()

    @Nested
    @DisplayName("When provided with flow that has parameterless primary constructor")
    inner class ParameterlessFlowSpec {

        inner class ParameterlessFlow : FlowLogic<String>() {
            override fun call(): String = "ParameterlessFlow result should be here."
        }

        private val flowClass = ParameterlessFlow::class.java
        private val flowName =
            "com.rewera.connectors.FlowClassConstructorParametersExtractorSpec\$ParameterlessFlowSpec\$ParameterlessFlow"

        @Test
        fun `when provided with empty Json should return empty list`() {
            val flowParameters = RpcStartFlowRequestParameters("{}")

            extractor.extractParameters(flowClass, flowParameters) shouldBe emptyList()
        }

        @Test
        fun `when provided with invalid Json should throw an exception`() {
            val flowParameters = RpcStartFlowRequestParameters("{\"This is\":\"not\" a Json")

            val exc = shouldThrow<JsonParseException> { extractor.extractParameters(flowClass, flowParameters) }
            exc.message shouldInclude "Unexpected character ('a' (code 97)): was expecting comma to separate Object entries"
        }

        @Test
        fun `when provided with non-empty Json should throw an exception`() {
            val flowParameters = RpcStartFlowRequestParameters("{\"testKey\":\"Test Value\",\"testKeyIntValue\":12345}")

            val exc = shouldThrow<IllegalArgumentException> { extractor.extractParameters(flowClass, flowParameters) }
            exc.message shouldBe "Additional parameters [testKey, testKeyIntValue] for flow [$flowName] found."
        }
    }

    @Nested
    @DisplayName("When provided with flow that has single-parameter primary constructor")
    inner class SingleParameterFlowSpec {

        inner class SingleParameterFlow(someParameter: String) : FlowLogic<String>() {
            override fun call(): String = "SingleParameterFlow result should be here."
        }

        private val flowClass = SingleParameterFlow::class.java
        private val flowName =
            "com.rewera.connectors.FlowClassConstructorParametersExtractorSpec\$SingleParameterFlowSpec\$SingleParameterFlow"

        @Test
        fun `when provided with empty Json should throw an exception`() {
            val flowParameters = RpcStartFlowRequestParameters("{}")

            val exc = shouldThrow<IllegalArgumentException> { extractor.extractParameters(flowClass, flowParameters) }
            exc.message shouldBe "Constructor parameters [someParameter] for flow [$flowName] were not provided."
        }

        @Test
        fun `when provided with invalid Json should throw an exception`() {
            val flowParameters = RpcStartFlowRequestParameters("{\"This is\":\"not\" a Json")

            val exc = shouldThrow<JsonParseException> { extractor.extractParameters(flowClass, flowParameters) }
            exc.message shouldInclude "Unexpected character ('a' (code 97)): was expecting comma to separate Object entries"
        }

        @Test
        fun `when provided with Json containing different key should throw an exception`() {
            val flowParameters = RpcStartFlowRequestParameters("{\"incorrectKey\":\"Test Value\"}")

            val exc = shouldThrow<IllegalArgumentException> { extractor.extractParameters(flowClass, flowParameters) }
            exc.message shouldBe "Constructor parameters [someParameter] for flow [$flowName] were not provided."
        }

        @Test
        fun `when provided with Json containing additional key should throw an exception`() {
            val flowParameters =
                RpcStartFlowRequestParameters("{\"someParameter\":\"Test Value\",\"extraParameter\":\"Test Value 2\"}")

            val exc = shouldThrow<IllegalArgumentException> { extractor.extractParameters(flowClass, flowParameters) }
            exc.message shouldBe "Additional parameters [extraParameter] for flow [$flowName] found."
        }

        @Test
        fun `when provided with Json containing correct key and value should return list with this value`() {
            val flowParameters = RpcStartFlowRequestParameters("{\"someParameter\":\"Test Value\"}")

            val result = extractor.extractParameters(flowClass, flowParameters)

            result.size shouldBe 1
            result.first() shouldBe "Test Value"
        }
    }

    @Nested
    @DisplayName("When provided with flow that has multiple parameters in primary constructor")
    inner class MultipleParametersFlowSpec {}
}