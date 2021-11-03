package com.p2m.example.none.pre_api

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.p2m.annotation.module.api.Launcher
import com.p2m.core.P2M
import com.p2m.core.moduleApi
import com.p2m.module.api.None

@Launcher
class NoneActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(View(this))


        val fragment = P2M.moduleApiOf(None::class.java)
            .launcher
            .fragmentOfTest
            .create()


    }

}