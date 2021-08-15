package com.music.app

open class Constants {

    companion object {
        const val READ_WRITE_PERMISSION_REQUEST_CODE = 100

        const val SERVICE_ACTION_ALREADY_PLAYING = "playing"
        const val SERVICE_ACTION_NOT_PLAYING = "not_playing"
        const val SERVICE_ACTION_NEXT = "next"
        const val SERVICE_ACTION_PREVIOUS = "previous"
        const val SERVICE_ACTION_PLAY = "play"
        const val SERVICE_ACTION_PAUSE = "pause"

        const val KEY_TITLE = "title"
        const val KEY_ARTIST = "artist"
        const val KEY_URI = "uri"
        const val KEY_IMAGE = "image"
        const val KEY_PLAYER_STATE = "player_state"
    }

}