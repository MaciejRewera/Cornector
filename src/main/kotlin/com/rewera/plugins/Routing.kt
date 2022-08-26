package com.rewera.plugins

import com.rewera.controllers.ControllersRegistry
import com.rewera.controllers.FlowStarterController
import com.rewera.models.RpcStartFlowRequest
import com.rewera.modules.Jackson.configure
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(controllersRegistry: ControllersRegistry) {
    install(ContentNegotiation) {
        jackson { this.configure() }
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/api/v1") {
            flowManagerRoutes()
            flowStarterRoutes(controllersRegistry.flowStarterController)
            vaultQueryRoutes()
        }

        route("/api/v2") {
            post("/startflowtyped") {}
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

        post("/startflow") {
            val rpcStartFlowRequest = call.receive<RpcStartFlowRequest>()
            call.respond(flowStarterController.startFlow(rpcStartFlowRequest))
        }


        get("/flowoutcomeforclientid/{clientid}") {
            val clientId = call.parameters["clientid"]

            clientId?.let { call.respond(flowStarterController.getFlowOutcomeForClientId(it)) }
                ?: throw MissingRequestParameterException("clientid")
        }

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
