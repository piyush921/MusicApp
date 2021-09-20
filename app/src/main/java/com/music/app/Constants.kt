package com.music.app

open class Constants {

    companion object {
        const val READ_WRITE_PERMISSION_REQUEST_CODE_SONGS = 100

        const val SERVICE_ACTION_ALREADY_PLAYING = "action_playing"
        const val SERVICE_ACTION_NOT_PLAYING = "action_not_playing"
        const val SERVICE_ACTION_NEXT = "action_next"
        const val SERVICE_ACTION_PREVIOUS = "action_previous"
        const val SERVICE_ACTION_PLAY = "action_play"
        const val SERVICE_ACTION_PAUSE = "action_pause"

        const val KEY_POSITION = "key_position"
        const val KEY_LIST = "key_list"
        const val KEY_PLAYER_STATE = "key_player_state"
        const val KEY_TEXT = "key_text"
        const val KEY_IMAGE = "key_image"
    }

}