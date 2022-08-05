package com.rewera.plugins

import com.rewera.connectors.CordaNodeConnector
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(cordaNodeConnector: CordaNodeConnector) {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        flowManagerRoutes()
        flowStarterRoutes(cordaNodeConnector)
        vaultQueryRoutes()
    }
}

fun Route.flowManagerRoutes() {
    route("/flowmanagerrpcops") {

        post("/killflow/{flowid}") {}

        get("/listactive") {}

        get("/getprotocolversion") {}
    }
}

fun Route.flowStarterRoutes(cordaNodeConnector: CordaNodeConnector) {
    route("/flowstarter") {

        get("/registeredflows") {
            call.respond(cordaNodeConnector.getRegisteredFlows())
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
