package com.p2m.gradle.task

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.bundling.Jar

class ApiJar extends Jar {

    @InputDirectory
    File inputKaptDirCollection

    @InputFiles
    FileTree inputKotlinCompilerOutput

    // java/lang/String
    List<String> exportApiClassPathList = new ArrayList<>()

}
