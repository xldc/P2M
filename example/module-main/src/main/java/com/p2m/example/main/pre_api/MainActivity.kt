package com.p2m.example.main.pre_api

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import com.p2m.core.P2M
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.example.main.R
import com.p2m.module.api.Account

@ApiLauncher("Main")
class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity_main)

        // 监听用户信息事件
        P2M.apiOf(Account::class.java)
            .event
            .loginInfo
            .observe(this, Observer { loginInfo ->
                findViewById<TextView>(R.id.main_content).apply {
                    text = """
                        Welcome ${loginInfo?.userName} ~
                    """.trimIndent()
                }
            })



        // 退出登录
        findViewById<Button>(R.id.main_btn_logout).setOnClickListener {
            P2M.apiOf(Account::class.java)
                .service
                .logout(this)
            finish()
        }

        // 测试事件的外部可变性
        P2M.apiOf(Account::class.java)
            .event
            .testMutableEventFromExternal
            .setValue(1)
    }

}