package com.p2m.core.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.annotation.module.api.ApiLauncherActivityResultContractFor
import com.p2m.core.internal.launcher.InternalActivityLauncher
import com.p2m.core.internal.launcher.InternalSafeIntent
import kotlin.reflect.KProperty

/**
 * A launcher of `Activity`.
 *
 * For example, has a `Activity` of login in `Account` module:
 * ```kotlin
 * @ApiLauncher("Login")
 * class LoginActivity:Activity()
 * ```
 *
 * then launch in `activity` of external module:
 * ```kotlin
 * val fragment = P2M.apiOf(Account)
 *      .launcher
 *      .activityOfLogin
 *      .launch(::startActivity)
 * ```
 *
 * @see ApiLauncher
 */
interface ActivityLauncher<I, O> : Launcher{

    class Delegate<I, O>(clazz: Class<*>, createActivityResultContractBlock: () -> ActivityResultContractP2MCompact<I, O>) {
        private val real by lazy(LazyThreadSafetyMode.NONE) {
            InternalActivityLauncher<I, O>(clazz, createActivityResultContractBlock)
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ActivityLauncher<I, O> = real
    }

    /**
     * Launch for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type) are null, though
     * they can be modified later in [onFillIntent].
     */
    fun launch(
        isGreenChannel: Boolean = false,
        interceptTimeoutSecond: Int = 10,
        launchBlock: LaunchActivityBlock
    )

    /**
     * Register a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * No need to explicitly pass in a instance of activity result contract during registration,
     * that instance will auto create, that type is implement class of
     * [ActivityResultContractP2MCompact] and use [ApiLauncherActivityResultContractFor]
     * annotated, and that must has a empty constructor.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun registerForActivityResult(activity: ComponentActivity, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncherP2MCompact<I, O>

    /**
     * Register a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * No need to explicitly pass in a instance of activity result contract during registration,
     * that instance will auto create, that type is implement class of
     * [ActivityResultContractP2MCompact] and use [ApiLauncherActivityResultContractFor]
     * annotated, and that must has a empty constructor.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun registerForActivityResult(fragment: Fragment, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncherP2MCompact<I, O>

    /**
     * Register a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * No need to explicitly pass in a instance of activity result contract during registration,
     * that instance will auto create, that type is implement class of
     * [ActivityResultContractP2MCompact] and use [ApiLauncherActivityResultContractFor]
     * annotated, and that must has a empty constructor.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun registerForActivityResult(activityResultRegistry: ActivityResultRegistry, key: String, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncherP2MCompact<I, O>
}

/**
 * A launcher of Activity Result.
 *
 * @see ApiLauncher
 */
class ActivityResultLauncherP2MCompact<I, O>(private val activityLauncher:ActivityLauncher<I, O>, private val activityResultLauncher: ActivityResultLauncher<I>) {

    /**
     * Launch a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * No need to explicitly pass in a instance of activity result contract during registration,
     * that instance will auto create, that type is implement class of
     * [ActivityResultContractP2MCompact] and use [ApiLauncherActivityResultContractFor]
     * annotated, and that must has a empty constructor.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun launch(input: I, options: ActivityOptionsCompat? = null) {
        activityResultLauncher.launch(input, options)
    }

    fun unregister() = activityResultLauncher.unregister()

    @Suppress("UNCHECKED_CAST")
    fun getContract(): ActivityResultContractP2MCompact<I, O> =
        activityResultLauncher.contract as ActivityResultContractP2MCompact<I, O>
}

abstract class ActivityResultContractP2MCompact<I, O> :
    ActivityResultContract<I, ActivityResultP2MCompact<O>>() {

    internal lateinit var activityClazz: Class<*>

    /**
     * Fill input into created intent.
     *
     * @param input - from input of [ActivityResultLauncher.launch].
     * @param intent - from returns of [createIntent].
     */
    abstract fun inputIntoCreatedIntent(input: I, intent: Intent)

    /**
     * Returns output of result, that will provide to [ActivityResultCallbackP2MCompact].
     *
     * @param resultCode - from [Activity.setResult] of owner activity.
     * @param intent - from [Activity.setResult] of owner activity.
     * @return - output of result.
     *
     * @see ActivityLauncher.registerForActivityResult
     * @see ActivityResultLauncherP2MCompact.launch
     */
    abstract fun outputFromResultIntent(resultCode: Int, intent: Intent?): O?

    final override fun createIntent(context: Context, input: I): Intent {
        val intent = if (input is Intent) InternalSafeIntent(input as Intent) else InternalSafeIntent()
        intent.setClassInternal(activityClazz)
        return intent.also { inputIntoCreatedIntent(input, it) }
    }

    final override fun parseResult(resultCode: Int, intent: Intent?): ActivityResultP2MCompact<O> =
        ActivityResultP2MCompact(resultCode, outputFromResultIntent(resultCode, intent))
}

class DefaultActivityResultContractP2MCompact : ActivityResultContractP2MCompact<Intent, Intent>() {

    override fun inputIntoCreatedIntent(input: Intent, intent: Intent) = Unit

    override fun outputFromResultIntent(resultCode: Int, intent: Intent?): Intent? = intent
}

data class ActivityResultP2MCompact<O>(val resultCode: Int, val output: O?)


/**
 * A callback for receive activity result.
 */
typealias ActivityResultCallbackP2MCompact<O> = (resultCode: Int, output: O?) -> Unit


/**
 * A block for launch service.
 */
typealias LaunchActivityBlock = (createdIntent: Intent) -> Unit
