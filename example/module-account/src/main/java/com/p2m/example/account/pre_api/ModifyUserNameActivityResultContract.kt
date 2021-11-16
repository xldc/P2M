package com.p2m.example.account.pre_api

import android.content.Intent
import com.p2m.annotation.module.api.ApiLauncherActivityResultContractFor
import com.p2m.core.launcher.ActivityResultContractP2MCompact
import com.p2m.core.launcher.ActivityResultP2MCompact

@ApiLauncherActivityResultContractFor("ModifyAccountName")
class ModifyUserNameActivityResultContract: ActivityResultContractP2MCompact<Unit, String>() {
    override fun inputFillToIntent(input: Unit, intent: Intent) {
        // NOTHING
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ActivityResultP2MCompact<String> =
        ActivityResultP2MCompact(resultCode, intent?.getStringExtra("result_new_user_name"))

}