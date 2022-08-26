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

        inner class ParameterlessTestFlow : FlowLogic<String>() {
            override fun call(): String = "ParameterlessTestFlow result should be here."
        }

        private val flowClass = ParameterlessTestFlow::class.java
        private val flowName =
            "com.rewera.connectors.FlowClassConstructorParametersExtractorSpec\$ParameterlessFlowSpec\$ParameterlessTestFlow"

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

        inner class SingleParameterTestFlow(someParameter: String) : FlowLogic<String>() {
            override fun call(): String = "SingleParameterTestFlow result should be here."
        }

        private val flowClass = SingleParameterTestFlow::class.java
        private val flowName =
            "com.rewera.connectors.FlowClassConstructorParametersExtractorSpec\$SingleParameterFlowSpec\$SingleParameterTestFlow"

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
    inner class MultipleParametersFlowSpec {

        inner class MultipleParametersTestFlow(
            firstParameter: String,
            secondParameter: Int,
            thirdParameter: String
        ) : FlowLogic<String>() {
            override fun call(): String = "MultipleParametersTestFlow result should be here."
        }

        private val flowClass = MultipleParametersTestFlow::class.java
        private val flowName =
            "com.rewera.connectors.FlowClassConstructorParametersExtractorSpec\$MultipleParametersFlowSpec\$MultipleParametersTestFlow"

        @Test
        fun `when provided with empty Json should throw an exception with all missing parameters listed`() {
            val flowParameters = RpcStartFlowRequestParameters("{}")

            val exc = shouldThrow<IllegalArgumentException> { extractor.extractParameters(flowClass, flowParameters) }
            exc.message shouldBe "Constructor parameters [firstParameter, secondParameter, thirdParameter] for flow [$flowName] were not provided."
        }

        @Test
        fun `when provided with Json containing additional key should throw an exception`() {
            val flowParameters = RpcStartFlowRequestParameters(
                "{\"firstParameter\":\"Test value 1\", \"extraParameter\":\"Test Value 2\", \"secondParameter\":1234567, \"thirdParameter\":\"Test value 3\"}"
            )

            val exc = shouldThrow<IllegalArgumentException> { extractor.extractParameters(flowClass, flowParameters) }
            exc.message shouldBe "Additional parameters [extraParameter] for flow [$flowName] found."
        }

        @Test
        fun `when provided with Json containing correct keys and values should return list with these values in order they appear in constructor`() {
            val flowParameters = RpcStartFlowRequestParameters(
                "{\"firstParameter\":\"Test value 1\", \"secondParameter\":1234567, \"thirdParameter\":\"Test value 3\"}"
            )

            val result = extractor.extractParameters(flowClass, flowParameters)

            result.size shouldBe 3
            result[0] shouldBe "Test value 1"
            result[1] shouldBe 1234567
            result[2] shouldBe "Test value 3"
        }

        @Test
        fun `when provided with Json containing correct keys and values in different order should return list with these values in order they appear in constructor`() {
            val flowParameters = RpcStartFlowRequestParameters(
                "{\"firstParameter\":\"Test value 1\", \"thirdParameter\":\"Test value 3\", \"secondParameter\":1234567}"
            )

            val result = extractor.extractParameters(flowClass, flowParameters)

            result.size shouldBe 3
            result[0] shouldBe "Test value 1"
            result[1] shouldBe 1234567
            result[2] shouldBe "Test value 3"
        }
    }
}