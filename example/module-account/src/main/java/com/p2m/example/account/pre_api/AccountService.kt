package com.p2m.example.account.pre_api

import android.content.Intent
import com.p2m.core.P2M
import com.p2m.annotation.module.api.*
import com.p2m.example.account.UserDiskCache
import com.p2m.module.api.Account

@Service
class AccountService{
    /**
     * 退出登录
     */
    fun logout(){
        val context = P2M.getContext()

        // 清除用户缓存
        P2M.moduleApiOf(Account::class.java)
            .event
            .apply {
                val userDiskCache = UserDiskCache(context)
                userDiskCache.clear()
                
                loginState.setValue(false)
                loginInfo.setValue(null)
            }

        // 跳转到登录界面
        P2M.moduleApiOf(Account::class.java)
            .launcher.newActivityIntentOfLoginActivity(context).run {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
    }
}
