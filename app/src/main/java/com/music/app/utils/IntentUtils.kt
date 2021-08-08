package com.music.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent

class IntentUtils {

    companion object {
        fun startIntent(context: Context, destination: Class<*>) {
            val intent = Intent(context, destination)
            context.startActivity(intent)
        }
        fun startIntentAndFinish(context: Activity, destination: Class<*>) {
            val intent = Intent(context, destination)
            context.startActivity(intent)
            context.finish()
        }
    }

}