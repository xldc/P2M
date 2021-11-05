package com.p2m.core.module.driver

interface Driver {

    fun considerOpenAwait()

    enum class State{
        NEW,
        OPENING,
        OPENED
    }
}