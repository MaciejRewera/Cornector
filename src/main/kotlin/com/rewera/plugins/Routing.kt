package com.rewera.plugins

import com.rewera.controllers.ControllersRegistry
import com.rewera.controllers.FlowManagerController
import com.rewera.controllers.FlowStarterController
import com.rewera.models.api.RpcStartFlowRequest
import com.rewera.modules.Jackson.configure
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureRouting(controllersRegistry: ControllersRegistry) {
    install(ContentNegotiation) {
        jackson { this.configure() }
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/api/v1") {
            flowManagerRoutes(controllersRegistry.flowManagerController)
            flowStarterRoutes(controllersRegistry.flowStarterController)
            vaultQueryRoutes()
        }

        route("/api/v2") {
            post("/startflowtyped") {}
        }
    }
}

fun Route.flowManagerRoutes(flowManagerController: FlowManagerController) {
    route("/flowmanagerrpcops") {

        post("/killflow/{flowid}") {
            val flowId = call.parameters["flowid"]

            flowId?.let { call.respond(flowManagerController.killFlow(it)) }
                ?: throw MissingRequestParameterException("flowid")
        }

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

        get("/flowoutcome/{flowid}") {
            val flowIdString = call.parameters["flowid"]

            val flowId = try {
                UUID.fromString(flowIdString)
            } catch (exc: IllegalArgumentException) {
                throw BadRequestException("flowId format is not correct.")
            }

            flowId?.let { call.respond(flowStarterController.getFlowOutcomeForFlowId(it)) }
                ?: throw MissingRequestParameterException("flowid")
        }

        get("/getprotocolversion") {}
    }
}

fun Route.vaultQueryRoutes() {
    route("/vaultqueryrpc") {

        post("/queryvault") {}

        get("/getprotocolversion") {}
    }
}
