package com.music.app.activities

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.music.app.Constants
import com.music.app.R
import com.music.app.databinding.ActivityPermissionDeniedBinding
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.music.app.base.BaseActivity


class PermissionDeniedActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityPermissionDeniedBinding
    private var text: String = ""
    private var image: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionDeniedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        initUi()
        setListeners()
    }

    private fun init() {
        val intent = intent
        text = intent.getStringExtra(Constants.KEY_TEXT).toString()
        image = intent.getIntExtra(Constants.KEY_IMAGE, 0)
    }

    private fun initUi() {
        binding.permissionDeniedText.text = text
        Glide.with(this).load(image).into(binding.permissionDeniedImage)
    }

    private fun setListeners() {
        binding.goToSettings.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.go_to_settings -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

}