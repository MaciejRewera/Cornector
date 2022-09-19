package com.rewera.connectors

import com.rewera.DynamicClassLoader
import com.rewera.testdata.TestData.MultipleParametersTestFlow
import com.rewera.testdata.TestData.ParameterlessTestFlow
import com.rewera.testdata.TestData.SingleParameterTestFlow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FlowClassBuilderSpec {

    private val classLoader = DynamicClassLoader()
    private val flowClassBuilder = FlowClassBuilder(classLoader)

    @Nested
    @DisplayName("FlowClassBuilder on buildFlowClass")
    inner class BuildFlowClassSpec {

        @Test
        fun `when provided with correct flowName should return flow class`() {
            val flowName1 = "com.rewera.testdata.TestData\$ParameterlessTestFlow"
            val flowName2 = "com.rewera.testdata.TestData\$SingleParameterTestFlow"
            val flowName3 = "com.rewera.testdata.TestData\$MultipleParametersTestFlow"

            flowClassBuilder.buildFlowClass(flowName1) shouldBe ParameterlessTestFlow::class.java
            flowClassBuilder.buildFlowClass(flowName2) shouldBe SingleParameterTestFlow::class.java
            flowClassBuilder.buildFlowClass(flowName3) shouldBe MultipleParametersTestFlow::class.java
        }

        @Test
        fun `when provided with incorrect flowName should throw an exception`() {
            val flowName = "not.a.valid.flow.name"

            val exc = shouldThrow<ClassNotFoundException> { flowClassBuilder.buildFlowClass(flowName) }
            exc.message shouldBe flowName
        }

    }

}