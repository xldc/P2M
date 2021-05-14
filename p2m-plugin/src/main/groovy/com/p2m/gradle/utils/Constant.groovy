package com.p2m.gradle.utils

class Constant {
    static final FILE_NAME_ANDROID_MANIFEST_XML= "AndroidManifest.xml"
    static final API_PATH_JAVA_SOURCE = "" +
            "generated${File.separator}" +
            "p2m${File.separator}" +
            "src${File.separator}" +
            "main"
    
    static final API_PATH_ANDROID_MANIFEST_XML = "" +
            "generated${File.separator}" +
            "p2m${File.separator}" +
            "src${File.separator}" +
            "main"
    
    static final FILE_GEN_XML_COMMENT = "<!--DON'T EDIT! auto gen by P2M.-->"
    static final FILE_GEN_CODE_COMMENT = "// DON'T EDIT! auto gen by P2M."
    static final P2M_GROUP_ID = "com.github.wangdaqi77.P2M"
    static final P2M_NAME_API = "p2m-core"
    static final P2M_NAME_ANNOTATION = "p2m-annotation"
    static final P2M_NAME_COMPILER = "p2m-compiler"
    static final P2M_VERSION = "1.0.0"
    static final P2M_MODULE_AAR_REPO_NAME = "p2mMavenRepository"
    static final P2M_PUBLISH_TASK_GROUP = "p2mPublish"
    static final P2M_MODULE_TASK_GROUP = "p2m"
}
