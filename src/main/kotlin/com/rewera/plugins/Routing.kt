package com.rewera.plugins

import com.rewera.connectors.CordaNodeConnector
import com.rewera.connectors.CornectorRpcOps
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        flowManagerRoutes()
        flowStarterRoutes()
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

fun Route.flowStarterRoutes() {
    route("/flowstarter") {

        get("/registeredflows") {}

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
