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
        const val SERVICE_ACTION_SEEK = "action_seek"
        const val SERVICE_ACTION_SHUFFLE_ON = "action_shuffle_on"
        const val SERVICE_ACTION_SHUFFLE_OFF = "action_shuffle_off"

        const val KEY_POSITION = "key_position"
        const val KEY_PROGRESS = "key_progress"
        const val KEY_LIST = "key_list"
        const val KEY_PLAYER_STATE = "key_player_state"
        const val KEY_TEXT = "key_text"
        const val KEY_IMAGE = "key_image"
        const val KEY_MEDIA_ID = "key_media_item"

        const val SHUFFLE_STATE_ENABLED = "1"
        const val SHUFFLE_STATE_DISABLED = "0"
    }

}