package com.music.app.utils

import java.util.concurrent.TimeUnit

open class NumberUtils {

    companion object {
        fun addZeroBeforeNumber(number: String): String {
            return if (number.length == 1) {
                "0$number"
            } else {
                number
            }
        }

        fun convertSecondsToTime(duration: Int): String {

            val minutes: Int = duration / 60
            val seconds: Int = duration % 60
            val hours: Int = minutes / 60

            return if(hours > 0) {
                "${addZeroBeforeNumber(hours.toString())} : " +
                        "${addZeroBeforeNumber(minutes.toString())} : " +
                        addZeroBeforeNumber(seconds.toString())
            } else {
                "${addZeroBeforeNumber(minutes.toString())} : " +
                        addZeroBeforeNumber(seconds.toString())
            }
        }
    }

}