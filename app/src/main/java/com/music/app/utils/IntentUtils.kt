package com.music.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.getSystemService

import android.app.ActivityManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService


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

        fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name.equals(service.service.className)) {
                    return true
                }
            }
            return false
        }
    }

}