package com.p2m.annotation.module.api

/**
 * A class uses this annotation will extract to `Api` area.
 *
 * NOTE: recommended only for data classes.
 *
 * For example, define the event holder for user info in `Account` module:
 * ```kotlin
 * @ApiUse
 * data class UserInfo(val uid: String)
 *
 * @ApiEvent
 * interface Event {
 *      @ApiEventField
 *      val userInfo : UserInfo
 * }
 * ```
 *
 * then uses when observe in external module:
 * ```kotlin
 * P2M.apiOf(Account)
 *      .event
 *      .userInfo
 *      .observe(Observe { userInfo: UserInfo ->
 *          ...
 *      })
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ApiUse