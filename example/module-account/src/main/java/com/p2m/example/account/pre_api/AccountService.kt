package com.p2m.example.account.pre_api

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
        
        P2M.moduleOf(Account::class.java)
            .event
            .apply {
                val userDiskCache = UserDiskCache(P2M.getContext())
                userDiskCache.clear()
                
                loginState.setValue(false)
                loginInfo.setValue(null)
            }
    }
}
