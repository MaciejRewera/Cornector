package com.rewera

import com.google.inject.Guice
import com.google.inject.Injector
import com.rewera.controllers.ControllersRegistry
import com.rewera.modules.MainModule
import com.rewera.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val injector = Guice.createInjector(MainModule(this))
    val controllersRegistry: ControllersRegistry = injector.instance()

    configureRouting(controllersRegistry)
}

inline fun <reified T> Injector.instance(): T = this.getInstance(T::class.java)