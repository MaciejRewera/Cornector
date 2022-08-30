package com.rewera.controllers

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.connectors.CordaNodeConnector
import java.util.*

@Singleton
class FlowManagerController @Inject constructor(private val cordaNodeConnector: CordaNodeConnector) {

    fun killFlow(flowId: String): Boolean = cordaNodeConnector.killFlow(UUID.fromString(flowId))
}