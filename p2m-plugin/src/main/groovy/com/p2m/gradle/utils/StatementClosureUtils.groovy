package com.p2m.gradle.utils

class StatementClosureUtils {

    static String getStatementMissingClosureTip(com.p2m.gradle.bean.settings.BaseProjectConfig moduleConfig, String closure, String paramsDesc, String closureDesc){
        if (moduleConfig instanceof com.p2m.gradle.bean.settings.ModuleProjectConfig) {
            return  "\nMissing closure:${closure}, Please check config in settings.gradle.\n" +
                    StatementClosureUtils.getStatementClosureTip(moduleConfig, closure, paramsDesc, closureDesc)
        }else if (moduleConfig instanceof com.p2m.gradle.bean.settings.AppProjectConfig){
            return  "\nMissing closure:${closure}, Please check config in settings.gradle.\n" +
                    StatementClosureUtils.getStatementClosureTip(moduleConfig, closure, paramsDesc, closureDesc)
        }else {
            return "unknown"
        }
    }



    static String getStatementClosureTip(com.p2m.gradle.bean.settings.BaseProjectConfig moduleConfig, String closure, String paramsDesc, String closureDesc){
        def paramsEmpty = paramsDesc == null
        if (moduleConfig instanceof com.p2m.gradle.bean.settings.ModuleProjectConfig) {
            if (paramsEmpty) {
                return  "p2m {\n" +
                        "   module(${moduleConfig._moduleNamed.get()}) {\n" +
                        "       ${closure}{\n" +
                        "           ${closureDesc}\n" +
                        "       }\n" +
                        "   }\n" +
                        "}\n"
            }else {
                return  "p2m {\n" +
                        "   module(${moduleConfig._moduleNamed.get()}) {\n" +
                        "       ${closure}(${paramsDesc}){\n" +
                        "           ${closureDesc}\n" +
                        "       }\n" +
                        "   }\n" +
                        "}\n"
            }


        }else if (moduleConfig instanceof com.p2m.gradle.bean.settings.AppProjectConfig){
            if (paramsEmpty) {
                return  "p2m {\n" +
                        "   app {\n" +
                        "       ${closure}{\n" +
                        "           ${closureDesc}\n" +
                        "       }\n" +
                        "   }\n" +
                        "}\n"
            }else {
                return  "p2m {\n" +
                        "   app {\n" +
                        "       ${closure}(${paramsDesc}){\n" +
                        "           ${closureDesc}\n" +
                        "       }\n" +
                        "   }\n" +
                        "}\n"
            }

        }else {
            return "unknown"
        }
    }


}
