package com.rewera.repositories

import com.google.inject.Singleton
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.litote.kmongo.KMongo

@Singleton
class FlowResultsRepository {

    private val client = KMongo.createClient()
    private val database = client.getDatabase("cornector")
    private val collection: MongoCollection<Document> = database.getCollection("FlowResults")

    fun getAll() = collection.find().toList()
}