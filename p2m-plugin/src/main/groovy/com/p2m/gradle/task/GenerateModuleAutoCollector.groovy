package com.p2m.gradle.task

import com.google.common.base.Charsets
import com.google.common.io.Closer
import com.p2m.gradle.utils.Constant
import com.squareup.javawriter.JavaWriter
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.lang.model.element.Modifier

@CacheableTask
class GenerateModuleAutoCollector extends DefaultTask {
    public static final String MODULE_AUTO_COLLECTOR = "ModuleAutoCollector"
    public static final String MODULE_AUTO_COLLECTOR_NAME = MODULE_AUTO_COLLECTOR + ".java"
    public static final String MODULE_AUTO_COLLECTOR_SUPER = "com.p2m.core.module.ModuleCollector"
    private ListProperty<List<String>> validDependenciesName = project.objects.listProperty(String.class)
    private Property<String> packageName = project.objects.property(String.class)

    private DirectoryProperty sourceOutputDir = project.objects.directoryProperty()

    @OutputDirectory
    DirectoryProperty getSourceOutputDir() {
        return sourceOutputDir
    }

    @Input
    ListProperty<String> getValidDependenciesName() {
        return validDependenciesName
    }

    @Input
    Property<String> getPackageName() {
        return packageName
    }

    @TaskAction
    void generate(){
        def packageName = packageName.get()
        File pkgFolder = new File(sourceOutputDir.get().asFile, packageName.replace(".", File.separator))
        if (!pkgFolder.isDirectory() && !pkgFolder.mkdirs()) {
            throw new RuntimeException("Failed to create " + pkgFolder.getAbsolutePath())
        }
        def moduleAutoCollectorFile = new File(pkgFolder, MODULE_AUTO_COLLECTOR_NAME)

        Closer closer = Closer.create()
        try {
            FileOutputStream fos = closer.register(new FileOutputStream(moduleAutoCollectorFile))
            OutputStreamWriter out = closer.register(new OutputStreamWriter(fos, Charsets.UTF_8))
            JavaWriter writer = closer.register(new JavaWriter(out))

            Set classModifiers = new HashSet()
            classModifiers.add(Modifier.PUBLIC)
            classModifiers.add(Modifier.FINAL)
            writer.emitJavadoc(Constant.FILE_GEN_CODE_COMMENT)
                    .emitPackage(packageName)
                    .beginType(MODULE_AUTO_COLLECTOR, "class", classModifiers, MODULE_AUTO_COLLECTOR_SUPER)


            Set constructorModifiers = new HashSet()
            constructorModifiers.add(Modifier.PUBLIC)
            writer.beginConstructor(constructorModifiers)
            validDependenciesName.get().forEach { moduleName ->
                writer.emitStatement("collect(\"%s\")", moduleName)
            }
            writer.endConstructor()
            writer.endType();
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }

    }

}
