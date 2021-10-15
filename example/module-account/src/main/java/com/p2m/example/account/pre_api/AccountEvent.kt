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
    @EventField(eventOn = EventOn.MAIN, mutableFromExternal = false)
    val loginInfo: LoginUserInfo?

    /**
     * 登录状态
     *
     * 状态发生变化时发送事件
     */
    @EventField // 等效于 @EventField(eventOn = EventOn.MAIN, mutableFromExternal = false)
    val loginState: Boolean

    /**
     * 登录成功
     *
     * 用户主动登录成功时发送事件
     */
    @EventField(eventOn = EventOn.BACKGROUND)
    val loginSuccess: Unit

    /**
     * mutableFromExternal = true，表示外部模块可以setValue和postValue
     *
     * 为了保证事件的安全性不推荐设置
     */
    @EventField(eventOn = EventOn.MAIN, mutableFromExternal = true)
    val testMutableEventFromExternal: Int

    val testAPT:Int     // 这个字段没有被注解，因此它将被过滤
}