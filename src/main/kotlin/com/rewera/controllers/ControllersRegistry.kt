package com.rewera.controllers

import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class ControllersRegistry @Inject constructor(
    val flowStarterController: FlowStarterController,
    val flowManagerController: FlowManagerController
)