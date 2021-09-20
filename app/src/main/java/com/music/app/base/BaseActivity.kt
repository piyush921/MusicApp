package com.music.app.base

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.music.app.Constants
import com.music.app.R
import com.music.app.activities.PermissionDeniedActivity
import com.music.app.songsRepository.SongsRepository


open class BaseActivity : AppCompatActivity() {

    lateinit var songsRepository: SongsRepository

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.READ_WRITE_PERMISSION_REQUEST_CODE_SONGS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("thisisdata", "permission granted")
                    songsRepository.getAllSongs()
                } else {
                    Log.d("thisisdata", "permission denied or null")
                    val intent = Intent(this, PermissionDeniedActivity::class.java)
                    intent.putExtra(Constants.KEY_TEXT, getString(R.string.permission_denied_read_write_text))
                    intent.putExtra(Constants.KEY_IMAGE, R.drawable.permission_read_write)
                    startActivity(intent)
                }
            }
            else -> {
                Log.d("thisisdata", "permission request code: $requestCode")
            }
        }
    }

}