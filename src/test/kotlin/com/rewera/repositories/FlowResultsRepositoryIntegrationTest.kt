package com.rewera.repositories

import com.mongodb.MongoWriteException
import com.rewera.models.FlowResult
import com.rewera.models.api.FlowStatus
import com.rewera.testdata.TestData.randomUuid
import com.rewera.testdata.TestData.randomUuidString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FlowResultsRepositoryIntegrationTest {

    private val repository = FlowResultsRepository()

    @BeforeEach
    fun setup() {
        repository.removeAll()
    }

    @Test
    fun `FlowResultsRepository when trying to insert 2 FlowResults with the same clientId should throw an exception`() {
        val clientId = "clientId"
        val flowResult1 =
            FlowResult(clientId = clientId, flowId = randomUuidString(), result = 123, status = FlowStatus.COMPLETED)
        val flowResult2 = flowResult1.copy(flowId = randomUuidString())

        repository.insert(flowResult1)
        val exc = shouldThrow<MongoWriteException> { repository.insert(flowResult2) }
        exc.message shouldInclude "WriteError{code=11000, message='E11000 duplicate key error collection: cornector.FlowResults index: clientId_1 dup key"
    }

    @Test
    fun `FlowResultsRepository when trying to insert 2 FlowResults with the same flowId should throw an exception`() {
        val flowResult1 = FlowResult(
            clientId = "clientId-1",
            flowId = randomUuidString(),
            result = 123,
            status = FlowStatus.COMPLETED
        )
        val flowResult2 = flowResult1.copy(clientId = "clientId-2")

        repository.insert(flowResult1)
        val exc = shouldThrow<MongoWriteException> { repository.insert(flowResult2) }
        exc.message shouldInclude "WriteError{code=11000, message='E11000 duplicate key error collection: cornector.FlowResults index: flowId_1 dup key"
    }

    @Test
    fun `FlowResultsRepository when trying to insert 2 FlowResults with flowId equal to null should insert both FlowResults`() {
        val flowResult1 = FlowResult(
            clientId = "clientId-1",
            flowId = null,
            result = 123,
            status = FlowStatus.COMPLETED
        )
        val flowResult2 = flowResult1.copy(clientId = "clientId-2")

        repository.insert(flowResult1)
        repository.insert(flowResult2)
    }

    @Test
    fun `FlowResultsRepository on findAll when there are no documents in Mongo should return empty list`() {
        repository.findAll() shouldBe emptyList()
    }

    @Test
    fun `FlowResultsRepository on findAll when there are multiple documents in Mongo should return all the documents`() {
        val flowResults = listOf(
            FlowResult(
                clientId = "clientId-1",
                flowId = randomUuidString(),
                result = 123,
                status = FlowStatus.COMPLETED
            ),
            FlowResult(clientId = "clientId-2", flowId = randomUuidString(), status = FlowStatus.RUNNING),
            FlowResult(
                clientId = "clientId-3",
                flowId = randomUuidString(),
                result = "Some String",
                status = FlowStatus.COMPLETED
            ),
        )
        flowResults.forEach { repository.insert(it) }

        val result = repository.findAll()

        result.size shouldBe 3
        result.forEach { flowResults shouldContain it }
    }

    @Test
    fun `FlowResultsRepository on insert should insert document to Mongo`() {
        val flowResult =
            FlowResult(clientId = "clientId", flowId = randomUuidString(), result = 123, status = FlowStatus.COMPLETED)

        repository.insert(flowResult)
        val result = repository.findAll()

        result.size shouldBe 1
        result.first() shouldBe flowResult
    }

    @Nested
    @DisplayName("FlowResultsRepository on findByFlowId")
    inner class FindByFlowIdTest {

        @Test
        fun `when there are NO documents in the DB should return null`() {
            repository.findByFlowId(randomUuid()) shouldBe null
        }

        @Test
        fun `when there are NO documents with given flowId in the DB should return null`() {
            repository.insert(FlowResult<Any>("clientId", flowId = randomUuidString()))

            repository.findByFlowId(randomUuid()) shouldBe null
        }

        @Test
        fun `when there is document with given flowId in the DB should return this document`() {
            val flowId = randomUuid()
            val flowResult = FlowResult<Any>("clientId", flowId = flowId.toString())
            repository.insert(flowResult)

            repository.findByFlowId(flowId) shouldBe flowResult
        }

        @Test
        fun `when there is document with given flowId and result in the DB should return this document with this result`() {
            val flowId = randomUuid()
            val flowResult = FlowResult("clientId", flowId = flowId.toString(), result = "Some String Result")
            repository.insert(flowResult)

            val result = repository.findByFlowId(flowId)

            result shouldBe flowResult
            result!!.result.shouldBeInstanceOf<String>()
        }

        @Test
        fun `when there are multiple documents in the DB should return document with matching flowId`() {
            val flowId = randomUuid()
            val flowResult = FlowResult<Any>("clientId-1", flowId = flowId.toString())
            val flowResults = listOf(
                flowResult,
                flowResult.copy(clientId = "clientId-2", flowId = randomUuidString()),
                flowResult.copy(clientId = "clientId-3", flowId = randomUuidString()),
            )
            flowResults.forEach { repository.insert(it) }

            repository.findByFlowId(flowId) shouldBe flowResult
        }

        @Test
        fun `when there are multiple documents in the DB should return them with correct result types`() {
            val flowId1 = randomUuid()
            val flowId2 = randomUuid()
            val flowId3 = randomUuid()
            val flowResult1 = FlowResult<Any>("clientId-1", flowId = flowId1.toString(), result = 123)
            val flowResult2 = FlowResult<Any>("clientId-2", flowId = flowId2.toString(), result = "Some String Result")
            val flowResult3 = FlowResult<Any>("clientId-3", flowId = flowId3.toString(), result = null)
            val flowResults = listOf(flowResult1, flowResult2, flowResult3)
            flowResults.forEach { repository.insert(it) }

            val result1 = repository.findByFlowId(flowId1)
            result1 shouldBe flowResult1
            result1!!.result.shouldBeInstanceOf<Int>()

            val result2 = repository.findByFlowId(flowId2)
            result2 shouldBe flowResult2
            result2!!.result.shouldBeInstanceOf<String>()

            val result3 = repository.findByFlowId(flowId3)
            result3 shouldBe flowResult3
            result3?.result shouldBe null
        }
    }


    @Nested
    @DisplayName("FlowResultsRepository on findByClientId")
    inner class FindByClientIdTest {

        @Test
        fun `when there are NO documents in the DB should return null`() {
            repository.findByClientId("clientId") shouldBe null
        }

        @Test
        fun `when there are NO documents with given clientId in the DB should return null`() {
            repository.insert(FlowResult<Any>("other-clientId"))

            repository.findByClientId("clientId") shouldBe null
        }

        @Test
        fun `when there is document with given clientId in the DB should return this document`() {
            val clientId = "clientId"
            val flowResult = FlowResult<Any>(clientId)
            repository.insert(flowResult)

            repository.findByClientId(clientId) shouldBe flowResult
        }

        @Test
        fun `when there is document with given clientId and result in the DB should return this document with this result`() {
            val clientId = "clientId"
            val flowResult = FlowResult(clientId, result = "Some String Result")
            repository.insert(flowResult)

            val result = repository.findByClientId(clientId)

            result shouldBe flowResult
            result!!.result.shouldBeInstanceOf<String>()
        }

        @Test
        fun `when there are multiple documents in the DB should return document with matching clientId`() {
            val clientId = "clientId-1"
            val flowResult = FlowResult<Any>(clientId)
            val flowResults = listOf(
                flowResult,
                FlowResult("clientId-2"),
                FlowResult("clientId-3")
            )
            flowResults.forEach { repository.insert(it) }

            repository.findByClientId(clientId) shouldBe flowResult
        }

        @Test
        fun `when there are multiple documents in the DB should return them with correct result types`() {
            val clientId1 = "clientId-1"
            val clientId2 = "clientId-2"
            val clientId3 = "clientId-3"
            val flowResult1 = FlowResult<Any>(clientId1, result = 123)
            val flowResult2 = FlowResult<Any>(clientId2, result = "Some String Result")
            val flowResult3 = FlowResult<Any>(clientId3, result = null)
            val flowResults = listOf(flowResult1, flowResult2, flowResult3)
            flowResults.forEach { repository.insert(it) }

            val result1 = repository.findByClientId(clientId1)
            result1 shouldBe flowResult1
            result1!!.result.shouldBeInstanceOf<Int>()

            val result2 = repository.findByClientId(clientId2)
            result2 shouldBe flowResult2
            result2!!.result.shouldBeInstanceOf<String>()

            val result3 = repository.findByClientId(clientId3)
            result3 shouldBe flowResult3
            result3?.result shouldBe null
        }
    }
}