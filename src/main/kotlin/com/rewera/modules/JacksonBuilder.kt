package com.rewera.modules

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.text.SimpleDateFormat

object JacksonBuilder {

    val jackson: ObjectMapper by lazy { ObjectMapper().configure() }

    fun ObjectMapper.configure(): ObjectMapper = this.registerModule(kotlinModule).apply {
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

}