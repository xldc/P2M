package com.p2m.annotation.module.api

/**
 * A class uses this annotation will generate a launch property for launcher of Api area
 * and provide to dependant module.
 *
 * Use `P2M.apiOf(${moduleName}::class.java).launcher` to get launcher, that `moduleName`
 * is defined in settings.gradle
 *
 * Supports:
 *  * Activity - will generate a property for launch activity,
 *  that property name is activityOf[name].
 *  * Fragment - will generate a property for create fragment,
 *  that property name is fragmentOf[name].
 *  * Service  - will generate a property for launch service,
 *  that property name is serviceOf[name].
 *
 * @property name - default is class name on annotated.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ApiLauncher(val name: String = NAME_NULL){
    companion object{
        private val NAME_REGEX = Regex( "^[A-Z][A-Za-z0-9]*$")
        const val NAME_NULL = ""

        fun checkName(launch: ApiLauncher, clazzName: String){
            check(launch.name == NAME_NULL || launch.name.matches(NAME_REGEX)) {
                "The ApiLauncher(name =\"${launch.name}\") at class $clazzName, that name must matches ${NAME_REGEX.pattern}, like Login or LoginPhone or LoginPhone1"
            }
        }
    }


}