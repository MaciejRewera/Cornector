package com.rewera.plugins

import com.rewera.controllers.ControllersRegistry
import com.rewera.controllers.FlowStarterController
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(controllersRegistry: ControllersRegistry) {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/api/v1") {
            flowManagerRoutes()
            flowStarterRoutes(controllersRegistry.flowStarterController)
            vaultQueryRoutes()
        }
    }
}

fun Route.flowManagerRoutes() {
    route("/flowmanagerrpcops") {

        post("/killflow/{flowid}") {}

        get("/listactive") {}

        get("/getprotocolversion") {}
    }
}

fun Route.flowStarterRoutes(flowStarterController: FlowStarterController) {
    route("/flowstarter") {

        get("/registeredflows") {
            call.respond(flowStarterController.getRegisteredFlows())
        }

        post("/startflow") {}

        get("/flowoutcomeforclientid/{clientid}") {}

        get("/flowoutcome/{flowid}") {}

        get("/getprotocolversion") {}
    }
}

fun Route.vaultQueryRoutes() {
    route("/vaultqueryrpc") {

        post("/queryvault") {}

        get("/getprotocolversion") {}
    }
}
