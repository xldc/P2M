package com.p2m.core.launcher

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.annotation.module.api.ApiLauncherActivityResultContractFor
import com.p2m.core.internal.launcher.InternalActivityLauncher
import com.p2m.core.internal.launcher.InternalSafeIntent
import kotlin.reflect.KProperty

/**
 * A launcher of Activity.
 *
 * @see ApiLauncher
 */
interface ActivityLauncher<I, O> {

    class Delegate<I, O>(clazz: Class<*>, createActivityResultContractBlock: () -> ActivityResultContractP2MCompact<I, O>) {
        private val real by lazy(LazyThreadSafetyMode.NONE) {
            InternalActivityLauncher<I, O>(clazz, createActivityResultContractBlock)
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ActivityLauncher<I, O> = real
    }

    /**
     * Create a instance of Intent for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with explicit calls.
     *
     * @return a instance of Intent.
     */
    fun createIntent(context: Context): Intent

    /**
     * Launch for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with [onFillIntent] calls.
     */
    fun launch(context: Context, onFillIntent: OnFillIntent? = null)

    /**
     * Launch for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with [onFillIntent] calls.
     */
    fun launch(activity: Activity, onFillIntent: OnFillIntent? = null)

    /**
     * Launch for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with [onFillIntent] calls.
     */
    fun launch(fragment: Fragment, onFillIntent: OnFillIntent? = null)

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
    fun registerForActivityResult(activity: ComponentActivity, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I>

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
    fun registerForActivityResult(fragment: Fragment, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I>

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
    fun registerForActivityResult(activityResultRegistry: ActivityResultRegistry, key: String, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I>

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
    fun launchForResult(activity: ComponentActivity, input: I, callback: ActivityResultCallbackP2MCompact<O>)

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
    fun launchForResult(fragment: Fragment, input: I, callback: ActivityResultCallbackP2MCompact<O>)
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
     * @param resultCode - from [Activity.setResult]] of owner activity.
     * @param intent - from [Activity.setResult]] of owner activity.
     * @return - output of result.
     *
     * @see ActivityLauncher.registerForActivityResult
     * @see ActivityLauncher.launchForResult
     */
    abstract fun outputFromResultIntent(resultCode: Int, intent: Intent?): O?

    final override fun createIntent(context: Context, input: I): Intent {
        val intent = if (input is Intent) InternalSafeIntent(input as Intent) else InternalSafeIntent()
        intent.setComponentInternal(ComponentName(context, activityClazz))
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
 * Fill for created intent.
 */
typealias OnFillIntent = Intent.() -> Unit

typealias ActivityResultCallbackP2MCompact<O> = (resultCode: Int, output: O?) -> Unit
