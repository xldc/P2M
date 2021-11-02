package com.p2m.core.app

import com.p2m.core.module.*

class App : Module<AppModuleApi>() {
    override val init: ModuleInit = EmptyModuleInit()
    override val api: AppModuleApi = AppModuleApi()

    init {
        _apiClazz = App::class.java
    }
}