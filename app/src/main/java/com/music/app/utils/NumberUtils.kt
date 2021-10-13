package com.music.app.utils

import java.text.CharacterIterator
import java.text.SimpleDateFormat
import java.text.StringCharacterIterator
import java.util.*

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
            //send duration in seconds (not milliseconds)
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

        fun convertSizeToFormat(size: Long): String? {
            var bytes = size
            if (-1000 < bytes && bytes < 1000) {
                return "$bytes B"
            }
            val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
            while (bytes <= -999950 || bytes >= 999950) {
                bytes /= 1000
                ci.next()
            }
            return java.lang.String.format("%.1f %cB", bytes / 1000.0, ci.current())
        }

        fun convertMillisecondsToDate(format: String, milliseconds: Long): String {
            val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
            return simpleDateFormat.format(Date(milliseconds))
        }
    }

}