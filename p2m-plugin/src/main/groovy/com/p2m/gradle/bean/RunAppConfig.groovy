package com.p2m.gradle.bean

class RunAppConfig {
    /**
     * 是否启用
     * false: module下的build.gradle中的whenRunApp { ... } 配置生效
     * true : 使用该配置，module下的build.gradle中的whenRunApp { ... } 配置失效
     */
    boolean enabled = false
    String applicationId
    String mainActivity
    int versionCode = 1
    String versionName = "1.0"

    void extend(RunAppConfig parent){
        if (enabled) {
            if (applicationId == null || applicationId.isEmpty()) applicationId = parent.applicationId
            if (mainActivity == null || mainActivity.isEmpty()) mainActivity = parent.mainActivity
            if (versionCode == 0) versionCode = parent.versionCode
            if (versionName == null || versionName.isEmpty()) versionName = parent.versionName
        }
    }

    String getMissingProperty(){
        if (enabled) {
            if (applicationId == null || applicationId.isEmpty()) return "applicationId"
            if (mainActivity == null || mainActivity.isEmpty()) return "mainActivity"
            if (versionCode == 0) return "versionCode"
            if (versionName == null || versionName.isEmpty()) return "versionName"
        }
        return null
    }
}