package com.music.app.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.lang.RuntimeException
import javax.net.ssl.StandardConstants


open class PrefsHelper(val context: Context) {

    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    init {
        val prefsFile: String = context.packageName
        sharedPreferences = context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit()
    }

    companion object {
        const val PLAYER_STATE = "player_state"

        const val PLAYER_STATE_PLAYING = "state_playing"
        const val PLAYER_STATE_PAUSE = "state_pause"
        const val PLAYER_STATE_STOP = "state_stop"
    }

    open fun delete(key: String?) {
        if (sharedPreferences!!.contains(key)) {
            editor!!.remove(key).commit()
        }
    }

    open fun savePref(key: String?, value: Any?) {
        delete(key)
        if (value is Boolean) {
            editor!!.putBoolean(key, (value as Boolean?)!!)
        } else if (value is Int) {
            editor!!.putInt(key, (value as Int?)!!)
        } else if (value is Float) {
            editor!!.putFloat(key, (value as Float?)!!)
        } else if (value is Long) {
            editor!!.putLong(key, (value as Long?)!!)
        } else if (value is String) {
            editor!!.putString(key, value as String?)
        } else if (value is Enum<*>) {
            editor!!.putString(key, value.toString())
        } else if (value != null) {
            throw RuntimeException("Attempting to save non-primitive preference")
        }
        editor!!.commit()
    }

    open fun saveSet(key: String?, set: Set<String?>?) {
        editor!!.putStringSet(key, set)
        editor!!.commit()
    }

    open fun getSet(key: String?, defValue: Set<String?>?): Set<String?>? {
        return sharedPreferences!!.getStringSet(key, defValue)
    }

    open fun getPref(key: String?): String? {
        return sharedPreferences!!.all[key].toString()
    }

    open fun <T> getPref(key: String?, defValue: T): T {
        val returnValue = sharedPreferences!!.all[key] as T?
        return returnValue ?: defValue
    }

    open fun isPrefExists(key: String?): Boolean {
        return sharedPreferences!!.contains(key)
    }

    open fun clearAllPref() {
        editor!!.clear()
        editor!!.commit()
    }

}