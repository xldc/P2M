package com.p2m.core.log

interface ILogger {
    fun log(level: Level, msg: String, throwable: Throwable? = null)
}