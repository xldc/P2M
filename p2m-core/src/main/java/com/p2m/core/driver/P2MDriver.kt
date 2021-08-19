package com.p2m.core.driver

import androidx.annotation.MainThread
/**
 * P2M Driver.
 */
interface P2MDriver {

    /**
     * Open drive.
     * Main thread will be blocked until all modules are initialized.
     */
    @MainThread
    fun open()
}