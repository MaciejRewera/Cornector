package com.rewera.repositories

import com.google.inject.Singleton
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.rewera.models.FlowResult
import com.rewera.models.api.FlowStatus
import com.rewera.modules.Jackson
import org.bson.Document
import org.bson.UuidRepresentation
import org.litote.kmongo.*
import org.litote.kmongo.util.KMongoUtil
import java.util.*


@Singleton
class FlowResultsRepository {

    // TODO: Make connection configurable
    private val connectionString = "mongodb://localhost:27017"
    private val collectionName = "FlowResults"

    private val clientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(connectionString))
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .codecRegistry(KMongoUtil.defaultCodecRegistry)
        .build()

    private val client = KMongo.createClient(clientSettings)
    private val database = client.getDatabase("cornector")
    private val collection: MongoCollection<FlowResult<*>> =
        database.getCollection(collectionName, FlowResult::class.java)

    init {
        collection.ensureIndex(
            FlowResult<*>::flowId,
            indexOptions = IndexOptions().unique(true)
                .partialFilterExpression(Document("flowId", Document("${MongoOperator.type}", "string")))
        )
        collection.ensureUniqueIndex(FlowResult<*>::clientId)
    }

    fun removeAll(): DeleteResult = collection.deleteMany(Document())

    fun <A> insert(flowResult: FlowResult<A>): InsertOneResult =
        collection.insertOne(Jackson.mapper.writeValueAsString(flowResult))

    fun findAll(): List<FlowResult<*>> = collection.find().toList()

    fun findByFlowId(flowId: UUID): FlowResult<*>? = collection.findOne(FlowResult<*>::flowId eq flowId.toString())

    fun findByClientId(clientId: String): FlowResult<*>? = collection.findOne(FlowResult<*>::clientId eq clientId)

    fun findByStatus(status: FlowStatus): List<FlowResult<*>> =
        collection.find(FlowResult<*>::status eq status).toList()
}