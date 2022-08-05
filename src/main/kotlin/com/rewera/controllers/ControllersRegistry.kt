package com.rewera.controllers

import com.google.inject.Inject

class ControllersRegistry @Inject constructor(
    val flowStarterController: FlowStarterController
)