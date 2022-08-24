package com.rewera

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.rewera.modules.JacksonBuilder.jackson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JacksonSpec {

    @Nested
    @DisplayName("Configured Jackson when expecting String")
    inner class ExpectedStringSpec {

        private val expectedClass = String::class.java

        @Test
        fun `and provided with String should return this String`() {
            val input = "\"1234567\""
            jackson.readValue(input, expectedClass) shouldBe "1234567"
        }

        @Test
        fun `and provided with Integer should throw an exception`() {
            val input = "1234567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Double number should throw an exception`() {
            val input = "123.4567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Boolean should throw an exception`() {
            val input = "true"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Object type should throw an exception`() {
            val input = "{\"someParam\":\"Test value\"}"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Array should throw an exception`() {
            val input = "[\"Test value 1\",\"Test value 2\",\"Test value 3\"]"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }
    }

    @Nested
    @DisplayName("Configured Jackson when expecting Integer")
    inner class ExpectedIntegerSpec {

        private val expectedClass = Int::class.java

        @Test
        fun `and provided with Integer should return this Integer`() {
            val input = "1234567"
            jackson.readValue(input, expectedClass) shouldBe 1234567
        }

        @Test
        fun `and provided with String should throw an exception`() {
            val input = "\"1234567\""
            shouldThrow<MismatchedInputException> {jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Double number should throw an exception`() {
            val input = "123.4567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Boolean should throw an exception`() {
            val input = "true"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Object type should throw an exception`() {
            val input = "{\"someParam\":\"Test value\"}"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Array should throw an exception`() {
            val input = "[\"Test value 1\",\"Test value 2\",\"Test value 3\"]"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }
    }

    @Nested
    @DisplayName("Configured Jackson when expecting Double")
    inner class ExpectedDoubleSpec {

        private val expectedClass = Double::class.java

        @Test
        fun `and provided with Double number should return this Double`() {
            val input = "123.4567"
            jackson.readValue(input, expectedClass) shouldBe 123.4567
        }

        @Test
        fun `and provided with Integer should return this Integer as Double`() {
            val input = "1234567"
            jackson.readValue(input, expectedClass) shouldBe 1234567.0
        }

        @Test
        fun `and provided with String should throw an exception`() {
            val input = "\"1234567\""
            shouldThrow<MismatchedInputException> {jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Boolean should throw an exception`() {
            val input = "true"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Object type should throw an exception`() {
            val input = "{\"someParam\":\"Test value\"}"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Array should throw an exception`() {
            val input = "[\"Test value 1\",\"Test value 2\",\"Test value 3\"]"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }
    }

    @Nested
    @DisplayName("Configured Jackson when expecting Boolean")
    inner class ExpectedBooleanSpec {

        private val expectedClass = Boolean::class.java

        @Test
        fun `and provided with Boolean should return this Boolean`() {
            val input = "true"
            jackson.readValue(input, expectedClass) shouldBe true
        }

        @Test
        fun `and provided with String should throw an exception`() {
            val input = "\"1234567\""
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Integer should throw an exception`() {
            val input = "1234567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Double number should throw an exception`() {
            val input = "123.4567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Array should throw an exception`() {
            val input = "[\"Test value 1\",\"Test value 2\",\"Test value 3\"]"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Object type should throw an exception`() {
            val input = "{\"someParam\":\"Test value\"}"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }
    }

    @Nested
    @DisplayName("Configured Jackson when expecting Object type")
    inner class ExpectedObjectSpec {

        private val expectedClass = TestDataClass::class.java

        @Test
        fun `and provided with Object type with correct field type should return this Object type`() {
            val input = "{\"someParam\":\"Test value\"}"
            jackson.readValue(input, expectedClass) shouldBe TestDataClass("Test value")
        }

        @Test
        fun `and provided with Object type with incorrect field type should throw an exception`() {
            val input = "{\"someParam\":1234567}"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with String should throw an exception`() {
            val input = "\"1234567\""
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Integer should throw an exception`() {
            val input = "1234567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Double number should throw an exception`() {
            val input = "123.4567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Boolean should throw an exception`() {
            val input = "true"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Array should return this Array`() {
            val input = "[\"Test value 1\",\"Test value 2\",\"Test value 3\"]"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }
    }

    @Nested
    @DisplayName("Configured Jackson when expecting Array")
    inner class ExpectedArraySpec {

        private val expectedClass = Array::class.java

        @Test
        fun `and provided with Array of Strings should return this Array`() {
            val input = "[\"Test value 1\",\"Test value 2\",\"Test value 3\"]"
            jackson.readValue(input, expectedClass) shouldBe arrayOf("Test value 1", "Test value 2", "Test value 3")
        }

        @Test
        fun `and provided with Array of Integers should return this Array`() {
            val input = "[1, 2, 3]"
            jackson.readValue(input, expectedClass) shouldBe arrayOf(1, 2, 3)
        }

        @Test
        fun `and provided with String should throw an exception`() {
            val input = "\"1234567\""
            shouldThrow<InvalidDefinitionException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Integer should throw an exception`() {
            val input = "1234567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Double number should throw an exception`() {
            val input = "123.4567"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Boolean should throw an exception`() {
            val input = "true"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }

        @Test
        fun `and provided with Object type should throw an exception`() {
            val input = "{\"someParam\":\"Test value\"}"
            shouldThrow<MismatchedInputException> { jackson.readValue(input, expectedClass) }
        }
    }

    @Nested
    @DisplayName("Configured Jackson when expecting Array of specific type")
    inner class ExpectedTypedArraySpec {

        @Test
        fun `and provided with TypeReference and Array of correct type should return Array of this type`() {
            val input = "[\"Test value 1\",\"Test value 2\",\"Test value 3\"]"
            val typeReference: TypeReference<Array<String>> = object : TypeReference<Array<String>>() {}

            jackson.readValue(input, typeReference) shouldBe arrayOf("Test value 1", "Test value 2", "Test value 3")
        }

        @Test
        fun `and provided with TypeReference and Array of incorrect type should throw an exception`() {
            val input = "[1, 2, 3]"
            val typeReference: TypeReference<Array<String>> = object : TypeReference<Array<String>>() {}

            shouldThrow<MismatchedInputException> { jackson.readValue(input, typeReference) }
        }
    }

    companion object {
        data class TestDataClass(val someParam: String)
    }
}
