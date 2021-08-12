package com.dirzaaulia.gamewish.modules.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ActivitySplashScreenBinding
import com.dirzaaulia.gamewish.modules.main.MainActivity
import com.dirzaaulia.gamewish.util.contentView

class SplashScreenActivity : AppCompatActivity() {

    private val binding : ActivitySplashScreenBinding by contentView(R.layout.activity_splash_screen)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation)
        binding.splashLogo.startAnimation(animation)

        val splashScreenTimeout = 3000
        val intent = Intent(this, MainActivity::class.java)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, splashScreenTimeout.toLong())

    }
}