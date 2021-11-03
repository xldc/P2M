package com.p2m.annotation.module.api

/**
 * Class annotated by [Launcher] will generate a launch function for launcher and provide
 * to dependant module.
 *
 * Supports: Activity, Fragment, Service
 *
 * @property
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Launcher(val name: String = NAME_NULL){
    companion object{
        private val NAME_REGEX = Regex( "^[A-Z][A-Za-z0-9]*$")
        const val NAME_NULL = ""

        fun checkName(launch: Launcher, clazzName: String){
            check(launch.name == NAME_NULL || launch.name.matches(NAME_REGEX)) {
                "The Launcher(name =\"${launch.name}\") at class $clazzName, that name must matches ${NAME_REGEX.pattern}, like Login or LoginPhone or LoginPhone1"
            }
        }
    }


}