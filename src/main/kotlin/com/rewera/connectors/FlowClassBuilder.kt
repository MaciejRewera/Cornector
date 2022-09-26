package com.rewera.connectors

import com.google.inject.Inject
import com.rewera.DynamicClassLoader
import net.corda.core.flows.FlowLogic

class FlowClassBuilder @Inject constructor(private val classLoader: DynamicClassLoader) {

    fun buildFlowClass(flowName: String): Class<out FlowLogic<*>> =
        Class.forName(flowName, true, classLoader) as Class<out FlowLogic<*>>
}