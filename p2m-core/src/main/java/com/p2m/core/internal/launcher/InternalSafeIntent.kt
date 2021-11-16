package com.p2m.core.internal.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent

internal class InternalSafeIntent : Intent {

    constructor() :super()

    constructor(o: Intent) :super(o)

    constructor(context: Context, cls: Class<*>) :super(context, cls)

    override fun setComponent(component: ComponentName?): Intent =
        throw IllegalStateException("NOT SUPPORT!")

    override fun setClassName(packageName: String, className: String): Intent =
        throw IllegalStateException("NOT SUPPORT!")

    override fun setClassName(packageContext: Context, className: String): Intent =
        throw IllegalStateException("NOT SUPPORT!")

    override fun setClass(packageContext: Context, cls: Class<*>): Intent =
        throw IllegalStateException("NOT SUPPORT!")

    fun setComponentInternal(component: ComponentName?) =
        super.setComponent(component)

    fun setClassNameInternal(packageName: String, className: String) =
        super.setClassName(packageName, className)

    fun setClassNameInternal(packageContext: Context, className: String) =
        super.setClassName(packageContext, className)

    fun setClassInternal(packageContext: Context, cls: Class<*>) =
        super.setClass(packageContext, cls)

}