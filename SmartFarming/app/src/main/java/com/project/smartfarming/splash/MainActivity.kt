package com.project.smartfarming.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.smartfarming.R
import com.project.smartfarming.dashboard.DashBoardActivity
import com.project.smartfarming.login.LoginActivity
import com.project.smartfarming.utlis.SharedPreferencesManager

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sharedPreferences = SharedPreferencesManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
// Initialize Views
        val circleView = findViewById<View>(R.id.circleView)
        val logoImage = findViewById<View>(R.id.logoImage)
        val flashLight = findViewById<View>(R.id.flashLight)

        // Step 1: Expand Circle
        circleView.visibility = View.VISIBLE
        val scaleX = ObjectAnimator.ofFloat(circleView, "scaleX", 1f, 10f)
        val scaleY = ObjectAnimator.ofFloat(circleView, "scaleY", 1f, 10f)
        scaleX.duration = 800
        scaleY.duration = 800

        // Step 2: Show Logo in Center
        logoImage.visibility = View.VISIBLE
        val fadeIn = ObjectAnimator.ofFloat(logoImage, "alpha", 0f, 1f).setDuration(500)
        val scaleUpX = ObjectAnimator.ofFloat(logoImage, "scaleX", 0.5f, 1f).setDuration(500)
        val scaleUpY = ObjectAnimator.ofFloat(logoImage, "scaleY", 0.5f, 1f).setDuration(500)

        // Step 3: Diagonal Flash Light Effect
        flashLight.visibility = View.VISIBLE
        val flashMoveX = ObjectAnimator.ofFloat(
            flashLight,
            "translationX",
            -200f,
            logoImage.x + logoImage.width / 2,
            1000f
        )
        val flashMoveY =
            ObjectAnimator.ofFloat(flashLight, "translationY", logoImage.y - 50f, logoImage.y + 50f)
        flashMoveX.duration = 700
        flashMoveY.duration = 700
        flashMoveX.interpolator = AccelerateDecelerateInterpolator()
        flashMoveY.interpolator = AccelerateDecelerateInterpolator()

        // Flash light transparency effect
        val flashFadeIn = ObjectAnimator.ofFloat(flashLight, "alpha", 0f, 1f).setDuration(200)
        val flashFadeOut = ObjectAnimator.ofFloat(flashLight, "alpha", 1f, 0f).setDuration(500)

        // Animation Sequence
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.play(scaleX).with(scaleY)
        animatorSet.play(fadeIn).with(scaleUpX).with(scaleUpY).after(scaleX)
        animatorSet.play(flashMoveX).with(flashMoveY).with(flashFadeIn).after(fadeIn)
        animatorSet.play(flashFadeOut).after(flashMoveX)

        animatorSet.start()

        // After Animation → Navigate to Next Screen
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val nextActivity = if (sharedPreferences.isUserLoggedIn()) {
                    DashBoardActivity::class.java
                } else {
                    LoginActivity::class.java
                }
                startActivity(Intent(this@MainActivity, nextActivity))
                finish()
            }
        })
    }
}
