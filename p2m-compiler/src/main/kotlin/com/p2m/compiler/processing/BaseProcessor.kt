package com.p2m.compiler.processing

import com.p2m.annotation.module.api.EventObservation
import com.p2m.compiler.utils.Logger
import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

abstract class BaseProcessor : AbstractProcessor() {
    companion object{
        const val OPTION_MODULE_NAME = "moduleName"

        const val PACKAGE_NAME_CORE = "com.p2m.core"
        const val PACKAGE_NAME_IMPL = "com.p2m.module.impl"
        const val PACKAGE_NAME_API = "com.p2m.module.api"
        const val PACKAGE_NAME_LAUNCHER = "com.p2m.module.launcher"
        const val CLASS_MODULE = "Module"
        const val CLASS_MODULE_EMPTY = "EmptyModule"
        const val CLASS_MODULE_API = "ModuleApi"
        const val CLASS_API_LAUNCHER = "ModuleLauncher"
        const val CLASS_API_SERVICE = "ModuleService"
        const val CLASS_API_EVENT = "ModuleEvent"
        const val CLASS_API_LAUNCHER_EMPTY = "EmptyModuleLauncher"
        const val CLASS_API_SERVICE_EMPTY = "EmptyModuleService"
        const val CLASS_API_EVENT_EMPTY = "EmptyModuleEvent"

        const val PACKAGE_NAME_MUTABLE_EVENT = "com.p2m.core.event.mutable"
        val LIVE_EVENT_INTERFACES = mapOf(
            EventObservation.LIKE_LIVE_DATA to "P2MLikeLiveDataLiveEvent",
            EventObservation.NO_STICKY to "P2MNoStickyLiveEvent",
            EventObservation.NO_LOSS to "P2MNoLossLiveEvent",
            EventObservation.NO_STICKY_NO_LOSS to "P2MNoStickyNoLossLiveEvent",
            EventObservation.MIXED to "P2MMixedLiveEvent"
        )
        val BACKGROUND_LIVE_EVENT_INTERFACES = mapOf(
            EventObservation.LIKE_LIVE_DATA to "P2MLikeLiveDataBackgroundLiveEvent",
            EventObservation.NO_STICKY to "P2MNoStickyBackgroundLiveEvent",
            EventObservation.NO_LOSS to "P2MNoLossBackgroundLiveEvent",
            EventObservation.NO_STICKY_NO_LOSS to "P2MNoStickyNoLossBackgroundLiveEvent",
            EventObservation.MIXED to "P2MMixedBackgroundLiveEvent"
        )
    }

    lateinit var options: Map<String, String>
    lateinit var elementUtils: Elements
    lateinit var typeUtils: Types
    lateinit var mFiler: Filer
    lateinit var mLogger: Logger
    lateinit var optionModuleName: String

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        mLogger = Logger(processingEnv.messager)
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        mFiler = processingEnv.filer
        options = processingEnv.options
        val optionModuleName = options[OPTION_MODULE_NAME]
        if (optionModuleName.isNullOrEmpty()) {
            mLogger.error(
                """
                    请在build.gradle添加以下内容：
                    kapt {
                            arguments {
                                arg("$OPTION_MODULE_NAME", "模块名称")
                            }
                    }
                """.trimIndent()
            )
        }else{
            this.optionModuleName = optionModuleName
        }

    }
}
