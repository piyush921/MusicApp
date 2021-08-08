package com.music.app.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.music.app.R
import com.music.app.base.BaseActivity
import com.music.app.databinding.ActivitySplashBinding
import com.music.app.utils.IntentUtils

class SplashActivity : BaseActivity() {

    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this).load(R.drawable.splash).into(binding.image)

        Handler(Looper.getMainLooper()).postDelayed({
            IntentUtils.startIntentAndFinish(this, HomeActivity::class.java)
        }, 3000)

    }
}