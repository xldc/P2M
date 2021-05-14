package com.p2m.gradle.bytecode

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleProject

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

public class P2MTransform extends Transform {

    private final static String CLASS_FILE_PATH_MODULE_GRAPH = "com/p2m/core/internal/module/ModuleGraph.class"
    HashMap<String, BaseProject> p2mProject
    boolean transformClassesDir

    P2MTransform(HashMap<String, BaseProject> p2mProject, boolean transformClassesDir){
        this.p2mProject = p2mProject
        this.transformClassesDir = transformClassesDir
    }


    @Override
    String getName() {
        return "p2mTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        //当前是否是增量编译
        boolean isIncremental = transformInvocation.isIncremental()
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        outputProvider.deleteAll()

        File moduleGraphJarOrigin = null
        File moduleGraphJarDest = null
        for (TransformInput input : inputs) {
            Collection<BaseProject> moduleProjects = includeModuleProjects()
            for (JarInput jarInput : input.getJarInputs()) {
                String destName = jarInput.getName()
                // rename jar files
                String hexName = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath())
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }

                File destFile = outputProvider.getContentLocation(
                        destName + "_" + hexName,
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR)

                // step 1 jar源复制到目标处
                FileUtils.copyFile(jarInput.getFile(), destFile)

                JarFile originJar = new JarFile(destFile)
                Enumeration<JarEntry> enumeration = originJar.entries()

                while (enumeration.hasMoreElements()) {
                    JarEntry originJarEntry = enumeration.nextElement()
                    String classFilePath = originJarEntry.getName()
                    // step 2.1 找到需要注入依赖关系的class
                    if (classFilePath.equals(CLASS_FILE_PATH_MODULE_GRAPH)) {
                        moduleGraphJarOrigin = jarInput.getFile()
                        moduleGraphJarDest = destFile
                        // step 2.2 找到需要被注入的class
                    } else if (classFilePath.startsWith("com/p2m/module/")) {
                        updateModuleStatusIfFindGenClass(classFilePath, moduleProjects)
                        // println(this, classFilePath + " > " + destFile.getAbsolutePath())
                    }
                }
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                // println(this,  "transform DirectoryInput" + directoryInput.getFile().getAbsolutePath())
                File dest = transformInvocation.getOutputProvider().getContentLocation(
                        directoryInput.getFile().getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY)
                // step 1 dir源复制到目标处
                FileUtils.copyDirectory(directoryInput.getFile(), dest)

                File classesDir = directoryInput.getFile()
                if (transformClassesDir) {
                    ModuleProject findModuleProject = findModuleProjectIfClassesDirOfModule(moduleProjects, classesDir.getAbsolutePath())
                    if (findModuleProject != null) {
                        findModuleProject.getProject().fileTree(classesDir, { files ->
                            // println(this, "transform classes dir of " + findModuleProject.getModuleNamed().get() + " as App")
                            files.include("com/p2m/module/**/*.class")
                            for (File file : files.getFiles()) {
                                String classAbsolutePath = file.getAbsolutePath()
                                // println(this, "file:" + classAbsolutePath)
                                int i = classAbsolutePath.indexOf("com/p2m/module/")
                                if (i != -1) {
                                    String classFilePath = classAbsolutePath.substring(i)
                                    // println(this, "transform classes dir, processed classFilePath: " + classFilePath)
                                    // step 2 标记生成的class
                                    updateModuleStatusIfFindGenClass(classFilePath, moduleProjects)
                                }
                            }
                        })
                    }
                }
            }
        }
        // step 3 根据模块的依赖关系和标记生成的class来操作字节码
        // println(this,  "transform transformModuleGraphJar")
        if (moduleGraphJarDest != null) {
            transformModuleGraphJar(moduleGraphJarOrigin, moduleGraphJarDest, transformInvocation.getContext().getTemporaryDir())
        }
    }

    private void updateModuleStatusIfFindGenClass(String classFilePath, Collection<BaseProject> moduleProjects) {
        String implModuleName = classFilePath.replace("com/p2m/module/impl/_", "").replace("Module.class", "")
        String moduleApiName = classFilePath.replace("com/p2m/module/api/", "").replace(".class", "")
        for (BaseProject moduleProject : moduleProjects) {
            ModuleProject baseProject = (ModuleProject) moduleProject
            if (
            !implModuleName.startsWith("com/")
                    && !implModuleName.endsWith("class")
                    && baseProject.getModuleName().equals(implModuleName)
            ) {
                // println(this,  "classFilePath：" + classFilePath)
                // println(this,  "implModuleName：" + implModuleName)
                baseProject.setExistModuleProxyImplClass(true)
                return
            }


            if (
            !moduleApiName.startsWith("com/")
                    && !moduleApiName.endsWith("class")
                    && baseProject.getModuleName().equals(moduleApiName)
            ) {
                // println(this,  "classFilePath：" + classFilePath)
                // println(this,  "moduleApiName：" + moduleApiName)
                baseProject.setExistApiClass(true)
                return
            }
        }

    }

    private Collection<BaseProject> includeModuleProjects() {
        Collection<BaseProject> moduleProjects= new ArrayList<>()
        for (Map.Entry<String, BaseProject> entry : this.p2mProject.entrySet()) {
            // 过滤非ModuleProject
            if (!(entry.getValue() instanceof ModuleProject)) continue
            moduleProjects.add(entry.getValue())
        }
        return moduleProjects
    }

    private ModuleProject findModuleProjectIfClassesDirOfModule(Collection<BaseProject> moduleProjects, String path) {
        Iterator<BaseProject> moduleIterator = moduleProjects.iterator()
        while (moduleIterator.hasNext()) {
            ModuleProject moduleProject = (ModuleProject) moduleIterator.next()
            boolean moduleOwner = path.startsWith(moduleProject.getProject().getBuildDir().getAbsolutePath())
            if (moduleOwner) {
                return moduleProject
            }
        }
        return null
    }

    private void transformModuleGraphJar(File origin, File dest, File tempDir) throws IOException {
        if (dest.exists()) dest.delete()

        JarFile originJar = new JarFile(origin)
        File tempJar = new File(tempDir, "temp_" + dest.getName())
        JarOutputStream tempJOS = new JarOutputStream(new FileOutputStream(tempJar))

        Enumeration<JarEntry> enumeration = originJar.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry originJarEntry = enumeration.nextElement()
            InputStream originEntryInputStream = originJar.getInputStream(originJarEntry)
            byte[] originEntryBytes = IOUtils.toByteArray(originEntryInputStream)
            String originEntryName = originJarEntry.getName()
            if (originEntryName.equals(CLASS_FILE_PATH_MODULE_GRAPH)) {
                // println(this, "transformModuleGraphJar " + originEntryName)
                // println(this, "transformModuleGraphJar dest:" + dest.getAbsolutePath())
                JarEntry tempEntry = new JarEntry(originEntryName)
                tempJOS.putNextEntry(tempEntry)

                ClassReader cr = new ClassReader(originEntryBytes)
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
                ModuleGraphClassAdapter adapter = new ModuleGraphClassAdapter(cw, p2mProject)
                cr.accept(adapter, 0)

                tempJOS.write(cw.toByteArray())
            }else {
                JarEntry tempEntry = new JarEntry(originEntryName)
                tempJOS.putNextEntry(tempEntry)
                tempJOS.write(originEntryBytes)
            }
            tempJOS.closeEntry()
        }

        tempJOS.close()
        originJar.close()
        FileUtils.copyFile(tempJar, dest)
    }
}