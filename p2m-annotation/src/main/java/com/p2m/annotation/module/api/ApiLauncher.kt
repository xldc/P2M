package com.p2m.annotation.module.api

/**
 * A class uses this annotation will generate a launch property for launcher of `Api` area.
 *
 * Use `P2M.apiOf(${moduleName}::class.java).launcher` to get `launcher` instance,
 * that `moduleName` is defined in settings.gradle.
 *
 * Supports:
 *  * Activity - will generate a property for launch activity,
 *  that property name is activityOf[launcherName], at the same time can use
 *  [ApiLauncherActivityResultContractFor] specify a activity result contract
 *  for this activity.
 *  * Fragment - will generate a property for create fragment,
 *  that property name is fragmentOf[launcherName].
 *  * Service  - will generate a property for launch service,
 *  that property name is serviceOf[launcherName].
 *
 * @property launcherName - default is class name on annotated.
 *
 * @see ApiLauncherActivityResultContractFor - specify a activity result contract for this
 * activity.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ApiLauncher(val launcherName: String = NAME_NO_SET){
    companion object{
        private val NAME_REGEX = Regex( "^[A-Z][A-Za-z0-9]*$")
        const val NAME_NO_SET = ""

        fun checkName(launch: ApiLauncher, clazzName: String){
            check(launch.launcherName == NAME_NO_SET || launch.launcherName.matches(NAME_REGEX)) {
                "The ApiLauncher(name = \"${launch.launcherName}\") at class $clazzName, that name must matches ${NAME_REGEX.pattern}, like Login or LoginPhone or LoginPhone1"
            }
        }
    }
}

/**
 * Uses this annotation specify a activity result contract for a activity of used [ApiLauncher].
 *
 * It is matched when the [launcherName] is the same as that [ApiLauncher.launcherName]
 * of a activity.
 *
 * @see ApiLauncher
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ApiLauncherActivityResultContractFor(vararg val launcherName: String)