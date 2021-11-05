package com.p2m.example.http

import kotlin.concurrent.thread

object Http {
    fun request(successBlock: (HttpData) -> Unit) {
        // 模拟网络请求
        thread {
           Thread.sleep(1500L)
            successBlock(HttpData("ok"))
        }
    }
}