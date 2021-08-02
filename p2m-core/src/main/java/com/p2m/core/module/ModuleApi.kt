package com.p2m.core.module

/**
 * A module project has one [ModuleApi] only, its provided to itself and the outside module.
 * The [ModuleApi] implementation class is auto generated, it's class name is defined module name
 * in settings.gradle.
 *
 * Contains [LAUNCHER] and [SERVICE], which are defined internally by annotation by the module.
 */
interface ModuleApi<LAUNCHER : ModuleLauncher, SERVICE : ModuleService, EVENT : ModuleEvent> {
     val launcher: LAUNCHER
     val service: SERVICE
     val event: EVENT
}

class EmptyModuleApi(
    override val launcher: EmptyModuleLauncher = EmptyModuleLauncher,
    override val service: EmptyModuleService = EmptyModuleService,
    override val event: EmptyModuleEvent = EmptyModuleEvent
) : ModuleApi<EmptyModuleLauncher, EmptyModuleService, EmptyModuleEvent>

interface ModuleLauncher

interface ModuleService

interface ModuleEvent

object EmptyModuleLauncher : ModuleLauncher

object EmptyModuleService : ModuleService

object EmptyModuleEvent : ModuleEvent