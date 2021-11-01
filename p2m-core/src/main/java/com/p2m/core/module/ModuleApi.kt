package com.p2m.core.module

/**
 * A [Module] has one [ModuleApi] only.
 *
 * It is like a window of a [Module].
 *
 * @property launcher - can launch Activity、Fragment、Service.
 * @property service  - can call some feature.
 * @property event    - can observe some event.
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