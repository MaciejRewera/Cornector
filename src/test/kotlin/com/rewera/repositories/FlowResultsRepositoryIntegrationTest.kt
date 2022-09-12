package com.rewera.repositories

import com.mongodb.MongoWriteException
import com.rewera.models.FlowResult
import com.rewera.models.api.FlowStatus
import com.rewera.testdata.TestData.randomUuid
import com.rewera.testdata.TestData.randomUuidString
import com.rewera.testdata.TestData.testClientId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldExist
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
        val clientId = testClientId
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
            FlowResult(
                clientId = testClientId,
                flowId = randomUuidString(),
                result = 123,
                status = FlowStatus.COMPLETED
            )

        repository.insert(flowResult)
        val result = repository.findAll()

        result.size shouldBe 1
        result.first() shouldBe flowResult
    }

    @Nested
    @DisplayName("FlowResultsRepository on insertClientId")
    inner class InsertClientIdTest {

        @Test
        fun `should insert document with clientId and status fields only into Mongo`() {
            repository.insertClientId(testClientId)

            val allResults = repository.findAll()

            val expectedFlowResult = FlowResult<Any>(clientId = testClientId, status = FlowStatus.RUNNING)
            allResults.size shouldBe 1
            allResults.first() shouldBe expectedFlowResult
        }

        @Test
        fun `when provided with the same clientId twice should throw an exception`() {
            repository.insertClientId(testClientId)

            val exc = shouldThrow<MongoWriteException> { repository.insertClientId(testClientId) }
            exc.message shouldInclude "WriteError{code=11000, message='E11000 duplicate key error collection: cornector.FlowResults index: clientId_1 dup key"
        }
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
            repository.insert(FlowResult<Any>(testClientId, flowId = randomUuidString()))

            repository.findByFlowId(randomUuid()) shouldBe null
        }

        @Test
        fun `when there is document with given flowId in the DB should return this document`() {
            val flowId = randomUuid()
            val flowResult = FlowResult<Any>(testClientId, flowId = flowId.toString())
            repository.insert(flowResult)

            repository.findByFlowId(flowId) shouldBe flowResult
        }

        @Test
        fun `when there is document with given flowId and result in the DB should return this document with this result`() {
            val flowId = randomUuid()
            val flowResult = FlowResult(testClientId, flowId = flowId.toString(), result = "Some String Result")
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
            repository.findByClientId(testClientId) shouldBe null
        }

        @Test
        fun `when there are NO documents with given clientId in the DB should return null`() {
            repository.insert(FlowResult<Any>(testClientId))

            repository.findByClientId("other-clientId") shouldBe null
        }

        @Test
        fun `when there is document with given clientId in the DB should return this document`() {
            val clientId = testClientId
            val flowResult = FlowResult<Any>(clientId)
            repository.insert(flowResult)

            repository.findByClientId(clientId) shouldBe flowResult
        }

        @Test
        fun `when there is document with given clientId and result in the DB should return this document with this result`() {
            val clientId = testClientId
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

    @Nested
    @DisplayName("FlowResultsRepository on findByStatus")
    inner class FindByStatusTest {

        @Test
        fun `when there are NO documents in the DB should return empty list`() {
            repository.findByStatus(FlowStatus.RUNNING) shouldBe emptyList()
            repository.findByStatus(FlowStatus.COMPLETED) shouldBe emptyList()
            repository.findByStatus(FlowStatus.FAILED) shouldBe emptyList()
        }

        @Test
        fun `when there are NO documents in the DB with given status should return empty list`() {
            repository.insert(FlowResult<Any>(testClientId, status = FlowStatus.RUNNING))

            repository.findByStatus(FlowStatus.COMPLETED) shouldBe emptyList()
            repository.findByStatus(FlowStatus.FAILED) shouldBe emptyList()
        }

        @Test
        fun `when there is single document in the DB with given status should return this document`() {
            val flowResult = FlowResult<Any>(testClientId, status = FlowStatus.RUNNING)
            repository.insert(flowResult)

            repository.findByStatus(FlowStatus.RUNNING) shouldBe listOf(flowResult)
        }

        @Test
        fun `when there are multiple documents in the DB with given status should return all of them`() {
            val flowResult = FlowResult<Any>("clientId-1", status = FlowStatus.RUNNING)
            val flowResults = listOf(
                flowResult,
                flowResult.copy(clientId = "clientId-2"),
                flowResult.copy(clientId = "clientId-3")
            )
            flowResults.forEach { repository.insert(it) }

            val result = repository.findByStatus(FlowStatus.RUNNING)

            result.size shouldBe 3
            result.forEach { flowResults shouldContain it }
        }

        @Test
        fun `when there are multiple documents in the DB should return only documents with given status`() {
            val flowResult = FlowResult<Any>("clientId-1", status = FlowStatus.RUNNING)
            val flowResults = listOf(
                flowResult,
                flowResult.copy(clientId = "clientId-2", status = FlowStatus.FAILED),
                flowResult.copy(clientId = "clientId-3", status = FlowStatus.COMPLETED),
                flowResult.copy(clientId = "clientId-4")
            )
            flowResults.forEach { repository.insert(it) }

            val result = repository.findByStatus(FlowStatus.RUNNING)

            result.size shouldBe 2
            result shouldExist { it.clientId == "clientId-1" }
            result shouldExist { it.clientId == "clientId-4" }
        }
    }

    @Nested
    @DisplayName("FlowResultsRepository on updateFlowId")
    inner class UpdateFlowIdTest {

        @Test
        fun `when there is NO FlowResult in the DB should NOT make any changes`() {
            val result = repository.updateFlowId(testClientId, randomUuid())

            result.matchedCount shouldBe 0L
            result.modifiedCount shouldBe 0L

            repository.findAll().size shouldBe 0
        }

        @Test
        fun `when there is NO FlowResult in the DB should NOT upsert new FlowResult`() {
            repository.updateFlowId(testClientId, randomUuid()).upsertedId shouldBe null
        }

        @Test
        fun `when there is FlowResult with different clientId in the DB should NOT make any changes`() {
            val flowResult = FlowResult<Any>(clientId = "clientId-1")
            repository.insert(flowResult)

            val result = repository.updateFlowId("clientId-2", randomUuid())

            result.matchedCount shouldBe 0L
            result.modifiedCount shouldBe 0L

            val flowResultsInDb = repository.findAll()
            flowResultsInDb.size shouldBe 1
            flowResultsInDb.first() shouldBe flowResult
        }

        @Test
        fun `when there is FlowResult with different clientId in the DB should NOT upsert new FlowResult`() {
            repository.insert(FlowResult<Any>(clientId = "clientId-1"))

            repository.updateFlowId("clientId-2", randomUuid()).upsertedId shouldBe null
        }

        @Test
        fun `when there is FlowResult with given clientId in the DB should update only flowId in this FlowResult`() {
            val flowResult = FlowResult<Any>(clientId = testClientId)
            repository.insert(flowResult)
            val flowId = randomUuid()

            val result = repository.updateFlowId(testClientId, flowId)

            result.matchedCount shouldBe 1L
            result.modifiedCount shouldBe 1L

            val flowResultsInDb = repository.findAll()
            val expectedFlowResult = flowResult.copy(flowId = flowId.toString())
            flowResultsInDb.size shouldBe 1
            flowResultsInDb.first() shouldBe expectedFlowResult
        }

        @Test
        fun `when there are multiple FlowResults in the DB should update only FlowResult with given clientId`() {
            val clientId = "clientId-1"
            val flowResult = FlowResult<Any>(clientId)
            val flowResults = listOf(
                flowResult,
                flowResult.copy(clientId = "clientId-2"),
                flowResult.copy(clientId = "clientId-3")
            )
            flowResults.forEach { repository.insert(it) }
            val flowId = randomUuid()

            val result = repository.updateFlowId(clientId, flowId)

            result.matchedCount shouldBe 1L
            result.modifiedCount shouldBe 1L

            val updatedFlowResultsInDb = repository.findAll().filter { it.clientId == clientId }
            val expectedFlowResult = flowResult.copy(flowId = flowId.toString())
            updatedFlowResultsInDb.size shouldBe 1
            updatedFlowResultsInDb.first() shouldBe expectedFlowResult
        }
    }

    @Nested
    @DisplayName("FlowResultsRepository on update")
    inner class UpdateStatusAndResultTest {

        @Test
        fun `when there is NO FlowResult in the DB should NOT make any changes`() {
            val result = repository.update(testClientId, FlowStatus.COMPLETED, "Some flow result")

            result.matchedCount shouldBe 0L
            result.modifiedCount shouldBe 0L

            repository.findAll().size shouldBe 0
        }

        @Test
        fun `when there is NO FlowResult in the DB should NOT upsert new FlowResult`() {
            repository.update(testClientId, FlowStatus.COMPLETED, "Some flow result").upsertedId shouldBe null
        }

        @Test
        fun `when there is FlowResult with different clientId in the DB should NOT make any changes`() {
            val flowResult = FlowResult<Any>(clientId = "clientId-1")
            repository.insert(flowResult)

            val result = repository.update("clientId-2", FlowStatus.COMPLETED, "Some flow result")

            result.matchedCount shouldBe 0L
            result.modifiedCount shouldBe 0L

            val flowResultsInDb = repository.findAll()
            flowResultsInDb.size shouldBe 1
            flowResultsInDb.first() shouldBe flowResult
        }

        @Test
        fun `when there is FlowResult with different clientId in the DB should NOT upsert new FlowResult`() {
            repository.insert(FlowResult<Any>(clientId = "clientId-1"))

            repository.update("clientId-2", FlowStatus.COMPLETED, "Some flow result").upsertedId shouldBe null
        }

        @Test
        fun `when there is FlowResult with given clientId in the DB should update only flowId in this FlowResult`() {
            val flowResult = FlowResult<Any>(clientId = testClientId)
            repository.insert(flowResult)
            val flowResultValue = "Some flow result"

            val result = repository.update(testClientId, FlowStatus.COMPLETED, flowResultValue)

            result.matchedCount shouldBe 1L
            result.modifiedCount shouldBe 1L

            val flowResultsInDb = repository.findAll()
            val expectedFlowResult = flowResult.copy(status = FlowStatus.COMPLETED, result = flowResultValue)
            flowResultsInDb.size shouldBe 1
            flowResultsInDb.first() shouldBe expectedFlowResult
        }

        @Test
        fun `when there are multiple FlowResults in the DB should update only FlowResult with given clientId`() {
            val clientId = "clientId-1"
            val flowResult = FlowResult<Any>(clientId)
            val flowResults = listOf(
                flowResult,
                flowResult.copy(clientId = "clientId-2"),
                flowResult.copy(clientId = "clientId-3")
            )
            flowResults.forEach { repository.insert(it) }
            val flowResultValue = "Some flow result"

            val result = repository.update(clientId, FlowStatus.COMPLETED, flowResultValue)

            result.matchedCount shouldBe 1L
            result.modifiedCount shouldBe 1L

            val updatedFlowResultsInDb = repository.findAll().filter { it.clientId == clientId }
            val expectedFlowResult = flowResult.copy(status = FlowStatus.COMPLETED, result = flowResultValue)
            updatedFlowResultsInDb.size shouldBe 1
            updatedFlowResultsInDb.first() shouldBe expectedFlowResult
        }
    }
}