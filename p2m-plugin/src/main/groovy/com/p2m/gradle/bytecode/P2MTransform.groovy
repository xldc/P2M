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
import com.p2m.gradle.bean.BaseProjectUnit
import com.p2m.gradle.bean.ModuleProjectUnit

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

@Deprecated
public class P2MTransform extends Transform {

    private final static String CLASS_FILE_PATH_MODULE_AUTO_REGISTER = "com/p2m/core/internal/module/ModuleAutoRegister.class"
    HashMap<String, BaseProjectUnit> p2mProject
    boolean transformClassesDir

    P2MTransform(HashMap<String, BaseProjectUnit> p2mProject, boolean transformClassesDir){
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

        File injectJarOrigin = null
        File injectJarDest = null
        for (TransformInput input : inputs) {
            Collection<BaseProjectUnit> moduleProjects = includeModuleProjects()
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
                    // step 2.1 找到被注入的class
                    if (classFilePath.equals(CLASS_FILE_PATH_MODULE_AUTO_REGISTER)) {
                        injectJarOrigin = jarInput.getFile()
                        injectJarDest = destFile
                        // step 2.2 找到需要注入的module class
                    } else if (classFilePath.startsWith("com/p2m/module/api/")) {
                        markIfFindModuleClass(classFilePath, moduleProjects)
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
                    ModuleProjectUnit findModuleProject = findModuleProjectIfClassesDirOfModule(moduleProjects, classesDir.getAbsolutePath())
                    if (findModuleProject != null) {
                        findModuleProject.getProject().fileTree(classesDir, { files ->
                            // println(this, "transform classes dir of " + findModuleProject.getModuleNamed().get() + " as App")
                            files.include("com/p2m/module/api/*.class")
                            for (File file : files.getFiles()) {
                                String classAbsolutePath = file.getAbsolutePath()
                                int i = classAbsolutePath.indexOf("com/p2m/module/api/")
                                if (i != -1) {
                                    String classFilePath = classAbsolutePath.substring(i)
                                    // step 2 标记生成的class
                                    markIfFindModuleClass(classFilePath, moduleProjects)
                                }
                            }
                        })
                    }
                }
            }
        }
        // step 3 根据模块的标记将模块注入到class
        if (injectJarDest != null) {
            transformInjectJar(injectJarOrigin, injectJarDest, transformInvocation.getContext().getTemporaryDir())
        }
    }

    private void markIfFindModuleClass(String classFilePath, Collection<BaseProjectUnit> moduleProjects) {
        String moduleName = classFilePath.replace("com/p2m/module/api/", "").replace(".class", "")
        for (BaseProjectUnit moduleProject : moduleProjects) {
            ModuleProjectUnit projectUnit = (ModuleProjectUnit) moduleProject
            boolean existModuleClass = !moduleName.startsWith("com/") &&
                    !moduleName.endsWith("class") &&
                    projectUnit.getModuleName() == moduleName
            if (existModuleClass) {
                // println(this,  "classFilePath：" + classFilePath)
                // println(this,  "moduleName：" + moduleName)
                projectUnit.existModuleClass = true
                return
            }
        }

    }

    private Collection<BaseProjectUnit> includeModuleProjects() {
        Collection<BaseProjectUnit> moduleProjects= new ArrayList<>()
        for (Map.Entry<String, BaseProjectUnit> entry : this.p2mProject.entrySet()) {
            // 过滤非ModuleProject
            if (!(entry.getValue() instanceof ModuleProjectUnit)) continue
            moduleProjects.add(entry.getValue())
        }
        return moduleProjects
    }

    private ModuleProjectUnit findModuleProjectIfClassesDirOfModule(Collection<BaseProjectUnit> moduleProjects, String path) {
        Iterator<BaseProjectUnit> moduleIterator = moduleProjects.iterator()
        while (moduleIterator.hasNext()) {
            ModuleProjectUnit moduleProject = (ModuleProjectUnit) moduleIterator.next()
            boolean moduleOwner = path.startsWith(moduleProject.getProject().getBuildDir().getAbsolutePath())
            if (moduleOwner) {
                return moduleProject
            }
        }
        return null
    }

    private void transformInjectJar(File origin, File dest, File tempDir) throws IOException {
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
            if (originEntryName == CLASS_FILE_PATH_MODULE_AUTO_REGISTER) {
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