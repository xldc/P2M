package com.p2m.gradle.utils

class Constant {
    public static final FILE_NAME_ANDROID_MANIFEST_XML= "AndroidManifest.xml"
    public static final API_PATH_JAVA_SOURCE = "" +
            "generated${File.separator}" +
            "p2m${File.separator}" +
            "src${File.separator}" +
            "main"

    public static final API_PATH_ANDROID_MANIFEST_XML = "" +
            "generated${File.separator}" +
            "p2m${File.separator}" +
            "src${File.separator}" +
            "main"

    public static final PLUGIN_ID_ANDROID_APP = "com.android.application"
    public static final PLUGIN_ID_ANDROID_LIBRARY = "com.android.library"
    public static final PLUGIN_ID_KOTLIN_ANDROID = "kotlin-android"
    public static final PLUGIN_ID_KOTLIN_KAPT = "kotlin-kapt"
    public static final PLUGIN_ID_MAVEN_PUBLISH = "maven-publish"

    public static final PROJECT_NAME_P2M_ANNOTATION = "p2m-annotation"
    public static final PROJECT_NAME_P2M_COMPILER = "p2m-compiler"
    public static final PROJECT_NAME_P2M_CORE = "p2m-core"

    public static final REPO_P2M_REMOTE = "https://jitpack.io"

    public static final LOCAL_PROPERTY_DEV_ENV = "p2m.dev"
    public static final LOCAL_PROPERTY_REPO_LOCAL = "p2m.repo.local"

    public static final FILE_GEN_XML_COMMENT = "<!--DON'T EDIT! auto gen by P2M.-->"
    public static final FILE_GEN_CODE_COMMENT = "// DON'T EDIT! auto gen by P2M."
    public static final P2M_GROUP_ID = "com.github.wangdaqi77.P2M"
    public static final P2M_NAME_API = "p2m-core"
    public static final P2M_NAME_ANNOTATION = "p2m-annotation"
    public static final P2M_NAME_COMPILER = "p2m-compiler"
    public static final P2M_VERSION = "0.0.1"
    public static final P2M_MODULE_AAR_REPO_NAME = "p2mMavenRepository"
    public static final P2M_PUBLISH_TASK_GROUP = "p2mPublish"
    public static final P2M_MODULE_TASK_GROUP = "p2m"
}
