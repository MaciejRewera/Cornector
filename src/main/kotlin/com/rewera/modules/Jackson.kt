package com.rewera.modules

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.text.SimpleDateFormat


object Jackson {

    val mapper: ObjectMapper by lazy { jacksonObjectMapper().configure().registerModule(strictStringDeserializerModule) }

    fun ObjectMapper.configure(): ObjectMapper =
        this
            .registerModule(kotlinModule)
            .configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false)
            .configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false)
            .apply {
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                dateFormat = dateFormatWithMillis
            }

    private val dateFormatWithMillis = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    private val kotlinModule = KotlinModule.Builder()
        .withReflectionCacheSize(512)
        .configure(KotlinFeature.NullToEmptyCollection, false)
        .configure(KotlinFeature.NullToEmptyMap, false)
        .configure(KotlinFeature.NullIsSameAsDefault, false)
        .configure(KotlinFeature.SingletonSupport, false)
        .configure(KotlinFeature.StrictNullChecks, false)
        .build()

    private val strictStringDeserializerModule =
        SimpleModule().addDeserializer(String::class.java, ForceStringDeserializer())

    class ForceStringDeserializer : JsonDeserializer<String>() {
        override fun deserialize(jsonParser: JsonParser?, deserializationContext: DeserializationContext?): String {
            if (jsonParser?.currentToken !== JsonToken.VALUE_STRING) {
                deserializationContext?.reportWrongTokenException(
                    String::class.java, JsonToken.VALUE_STRING,
                    "Attempted to parse token %s to String",
                    jsonParser?.currentToken
                )
            }
            return jsonParser?.valueAsString!!
        }
    }

}