package com.music.app.utils

open class NumberUtils {

    companion object {
        fun addZeroBeforeNumber(number: String): String {
            return if (number.length == 1) {
                "0${number}"
            } else {
                number
            }
        }
    }

}