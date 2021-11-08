package com.p2m.core.internal.log

import com.p2m.core.module.Module

internal inline val Class<out Module<*>>.moduleName: String
    get() = simpleName.removePrefix("_")