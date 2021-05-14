package com.p2m.core

import com.p2m.core.log.ILogger

class P2MConfigManager private constructor() {

    companion object{
        internal fun newInstance(): P2MConfigManager {
            return P2MConfigManager()
        }
    }

    internal var logger: ILogger? = null

    fun setLogger(logger: ILogger): P2MConfigManager {
        this.logger = logger
        return this
    }
}