package com.p2m.gradle.utils

import com.p2m.gradle.bean.settings.BaseProjectConfig
import com.p2m.gradle.bean.settings.AppProjectConfig
import com.p2m.gradle.bean.settings.BaseSupportRunAppConfig
import com.p2m.gradle.extension.settings.P2MConfig
import com.p2m.gradle.bean.settings.ModuleProjectConfig

class StatementPropertyUtils {

    static String getStatementMissingPropertyTip(BaseProjectConfig moduleConfig, String property){
        if (moduleConfig instanceof ModuleProjectConfig) {
            return  "\nMissing property:${property}, Please check config in settings.gradle.\n" +
                    StatementPropertyUtils.getStatementPropertyTip(moduleConfig, property)
        }else if (moduleConfig instanceof AppProjectConfig){
            return  "\nMissing property:${property}, Please check config in settings.gradle.\n" +
                    StatementPropertyUtils.getStatementPropertyTip(moduleConfig, property)
        }else {
            return "unknown"
        }
    }

    static String getAppRunConfigStatementMissingPropertyTip(BaseSupportRunAppConfig runAppConfig, String property){
        if (runAppConfig instanceof P2MConfig) {
            return  "\nMissing property:${property}, Please check config in settings.gradle.\n" +
                    StatementPropertyUtils.getAppRunConfigStatementPropertyTip(runAppConfig, property)
        }else if (runAppConfig instanceof ModuleProjectConfig) {
            return  "\nMissing property:${property}, Please check config in settings.gradle.\n" +
                    StatementPropertyUtils.getAppRunConfigStatementPropertyTip(runAppConfig, property)
        }else if (runAppConfig instanceof AppProjectConfig){
            return  "\nMissing property:${property}, Please check config in settings.gradle.\n" +
                    StatementPropertyUtils.getAppRunConfigStatementPropertyTip(runAppConfig, property)
        }else {
            return "unknown"
        }
    }

    static String getStatementPropertyTip(BaseProjectConfig moduleConfig, String property){
        if (moduleConfig instanceof ModuleProjectConfig) {
            return  "p2m {\n" +
                    "   module(${moduleConfig._moduleNamed.get()}) {\n" +
                    "       ${property} = your value\n" +
                    "       ...\n" +
                    "   }\n" +
                    "}\n"
        }else if (moduleConfig instanceof AppProjectConfig){
            return  "p2m {\n" +
                    "   app {\n" +
                    "       ${property} = your value\n" +
                    "       ...\n" +
                    "   }\n" +
                    "}\n"
        }else {
            return "unknown"
        }
    }


    static String getAppRunConfigStatementPropertyTip(BaseSupportRunAppConfig runAppConfig, String property) {
        if (runAppConfig instanceof P2MConfig) {
            return  "p2m {\n" +
                    "   runAppConfig {\n" +
                    "       ${property} = your value\n" +
                    "       ...\n" +
                    "   }\n" +
                    "}\n"
        }else if (runAppConfig instanceof ModuleProjectConfig) {
            return  "p2m {\n" +
                    "   module(${runAppConfig._moduleNamed.get()}) {\n" +
                    "       runAppConfig {\n" +
                    "           ${property} = your value\n" +
                    "           ...\n" +
                    "       }\n" +
                    "   }\n" +
                    "}\n"
        }else if (runAppConfig instanceof AppProjectConfig){
            return  "p2m {\n" +
                    "   app {\n" +
                    "       runAppConfig {\n" +
                    "           ${property} = your value\n" +
                    "           ...\n" +
                    "       }\n" +
                    "   }\n" +
                    "}\n"
        }else {
            return "unknown"
        }
    }
}
