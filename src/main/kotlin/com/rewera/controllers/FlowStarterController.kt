package com.rewera.controllers

import com.google.inject.Inject
import com.google.inject.Singleton
import com.rewera.connectors.CordaNodeConnector

@Singleton
class FlowStarterController @Inject constructor(private val cordaNodeConnector: CordaNodeConnector) {

    fun getRegisteredFlows(): List<String> = cordaNodeConnector.getRegisteredFlows()

}
