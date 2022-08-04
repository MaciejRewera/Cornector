package com.rewera

import com.google.inject.Guice
import com.rewera.modules.ConnectorsModule
import com.rewera.modules.MainModule
import com.rewera.plugins.configureRouting
import com.rewera.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    Guice.createInjector(MainModule(this), ConnectorsModule())

    configureSerialization()
    configureRouting()
}