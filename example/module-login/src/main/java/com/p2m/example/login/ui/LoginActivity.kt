package com.p2m.example.login.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.p2m.core.P2M
import com.p2m.annotation.module.api.Launcher
import com.p2m.module.api.Login
import com.p2m.example.login.LoginUserInfo

import com.p2m.example.login.R
import java.util.*

/**
 * 登录Activity
 */
@Launcher
class LoginActivity : AppCompatActivity() {

    var loading: ProgressBar? = null
    var login: Button? = null
    var username: EditText? = null
    var password: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_activity_login)
        initView()

        login?.setOnClickListener {
            login(username?.text.toString(), password?.text.toString())
        }
    }

    private fun login(userName: String, pwd: String) {
        loading?.visibility = View.VISIBLE
        loading?.postDelayed({
            // 模拟登录成功
            onLoginSuccess(userName, UUID.randomUUID().toString())
        }, 2000)
    }

    private fun onLoginSuccess(userName:String, id:String) {
        loading?.visibility = View.GONE

        // 用户信息保存到sp
        saveToSp(userName, id)
        val loginModule = P2M.moduleOf(Login::class.java)

        // 用户信息
        loginModule.event.loginInfo.setValue(LoginUserInfo().apply {
            this.userId = id
            this.userName = userName
        })
        // 登录状态
        loginModule.event.loginState.setValue(true)
        // 跳转主界面
        loginModule.event.loginSuccessJumpMain.setValue(true)

        finish()
    }

    private fun saveToSp(userName: String, id: String) {
        val sp = getSharedPreferences("login_user", Context.MODE_PRIVATE)
        sp.edit().apply {
            putBoolean("login_state", true)
            putString("login_user", userName)
            putString("login_id", id)
            apply()
        }
    }

    private fun initView() {
        loading = findViewById<ProgressBar>(R.id.loading)
        username = findViewById<EditText>(R.id.username)
        password = findViewById<EditText>(R.id.password)
        login = findViewById<Button>(R.id.login)
        username?.afterTextChanged {
            login?.isEnabled = username?.text?.length?:0 > 2 && password?.text?.length?:0  > 2
        }
        password?.afterTextChanged {
            login?.isEnabled = username?.text?.length?:0  > 2 && password?.text?.length?:0  > 2
        }

    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}