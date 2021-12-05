package com.music.app.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.music.app.Constants
import com.music.app.R
import com.music.app.activities.PermissionDeniedActivity
import com.music.app.database.SongsDatabase
import com.music.app.repository.SongsRepository


open class BaseActivity : AppCompatActivity() {

    private val TAG : String = BaseActivity::class.java.simpleName
    lateinit var songsRepository: SongsRepository
    lateinit var songsDatabase: SongsDatabase

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.READ_WRITE_PERMISSION_REQUEST_CODE_SONGS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    songsRepository.getAllSongs()
                } else {
                    val intent = Intent(this, PermissionDeniedActivity::class.java)
                    intent.putExtra(Constants.KEY_TEXT, getString(R.string.permission_denied_read_write_text))
                    intent.putExtra(Constants.KEY_IMAGE, R.drawable.permission_read_write)
                    startActivity(intent)
                }
            }
            else -> {
                Log.d(TAG, "permission request code: $requestCode")
            }
        }
    }

}