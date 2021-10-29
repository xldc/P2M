package com.p2m.core.app

import com.p2m.core.module.*

/**
 * 需要transform时追加其他顶级模块
 */
class App : Module<AppModuleApi, EmptyModuleInit>() {
    override val init: EmptyModuleInit = EmptyModuleInit()
    override val api: AppModuleApi = AppModuleApi()

    init {
        _apiClazz = App::class.java
    }
}