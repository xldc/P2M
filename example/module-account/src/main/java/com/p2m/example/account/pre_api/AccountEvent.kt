package com.p2m.example.account.pre_api

import com.p2m.annotation.module.api.Event
import com.p2m.annotation.module.api.EventField
import com.p2m.annotation.module.api.EventOn


@Event
interface AccountEvent{
    /**
     * 登录用户信息
     *
     * 信息发生变化时发送事件
     */
    // @EventField 默认
    val loginInfo: LoginUserInfo?

    /**
     * 登录状态
     *
     * 状态发生变化时发送事件
     */
    @EventField
    val loginState: Boolean

    /**
     * 登录成功
     *
     * 用户主动登录成功时发送事件
     */
    @EventField(eventOn = EventOn.BACKGROUND)
    val loginSuccess: Unit
}