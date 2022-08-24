package com.rewera.connectors

import com.fasterxml.jackson.databind.JsonNode
import com.rewera.models.RpcStartFlowRequestParameters
import com.rewera.modules.Jackson
import net.corda.core.flows.FlowLogic
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

class FlowClassConstructorParametersExtractor {

    fun <T> extractParameters(
        flowClass: Class<out FlowLogic<T>>,
        flowParameters: RpcStartFlowRequestParameters
    ): List<Any> {
        fun validateForMissingParams(
            constructorParameters: List<KParameter>,
            mapOfActualParams: Map<String, JsonNode>
        ) {
            val mandatoryParameterNames = constructorParameters.filterNot { it.isOptional }.map { it.name!! }
            val missingParameterKeys = mandatoryParameterNames.minus(mapOfActualParams.keys)

            if (missingParameterKeys.isNotEmpty())
                throw IllegalArgumentException("Constructor parameters [${missingParameterKeys.joinToString(", ")}] for flow [${flowClass.name}] were not provided.")
        }

        fun validateForAdditionalParams(
            constructorParameters: List<KParameter>,
            mapOfActualParams: Map<String, JsonNode>
        ) {
            val additionalParameterKeys = mapOfActualParams.keys.minus(constructorParameters.map { it.name!! }.toSet())

            if (additionalParameterKeys.isNotEmpty())
                throw IllegalArgumentException("Additional parameters [${additionalParameterKeys.joinToString(", ")}] for flow [${flowClass.name}] found.")
        }

        val constructorParameters = flowClass.kotlin.primaryConstructor!!.valueParameters
        val mapOfActualParams: Map<String, JsonNode> =
            Jackson.mapper.readTree(flowParameters.parametersInJson).fields().asSequence().associate { it.toPair() }

        validateForMissingParams(constructorParameters, mapOfActualParams)
        validateForAdditionalParams(constructorParameters, mapOfActualParams)

        return constructorParameters.mapNotNull {
            if (it.isOptional)
                extractSingleOptionalParameter(it, mapOfActualParams)
            else
                extractSingleParameter(it, mapOfActualParams)
        }
    }

    private fun extractSingleParameter(parameter: KParameter, mapOfActualParams: Map<String, JsonNode>): Any {
        val parameterClass: Class<*> = Class.forName(parameter.type.jvmErasure.javaObjectType.typeName) as Class<*>
        val rawValue = mapOfActualParams[parameter.name!!]
            ?: throw NoSuchElementException("Cannot find parameter [${parameter.name!!}]")

        return Jackson.mapper.treeToValue(rawValue, parameterClass)
    }

    private fun extractSingleOptionalParameter(parameter: KParameter, mapOfActualParams: Map<String, JsonNode>): Any? {
        val parameterClass: Class<*> = Class.forName(parameter.type.jvmErasure.javaObjectType.typeName) as Class<*>

        return mapOfActualParams[parameter.name!!]
            ?.let { Jackson.mapper.treeToValue(it, parameterClass) }
    }
}
