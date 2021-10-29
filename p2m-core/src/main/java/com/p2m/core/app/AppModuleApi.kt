package com.p2m.core.app

import com.p2m.core.internal.app.AppModuleServiceImpl
import com.p2m.core.module.EmptyModuleEvent
import com.p2m.core.module.EmptyModuleLauncher
import com.p2m.core.module.ModuleApi
import com.p2m.core.module.ModuleService

internal const val APP_MODULE_NAME = "App"

interface AppModuleService : ModuleService

class AppModuleApi(
    override val launcher: EmptyModuleLauncher = EmptyModuleLauncher,
    override val service: AppModuleService = AppModuleServiceImpl(),
    override val event: EmptyModuleEvent = EmptyModuleEvent
) : ModuleApi<EmptyModuleLauncher, AppModuleService, EmptyModuleEvent>