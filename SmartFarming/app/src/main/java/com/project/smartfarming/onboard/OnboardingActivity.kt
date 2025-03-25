package com.project.smartfarming.onboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.project.smartfarming.R
import com.project.smartfarming.dashboard.DashBoardActivity
import com.project.smartfarming.databinding.ActivityOnboardingBinding
import me.relex.circleindicator.CircleIndicator3

class OnboardingActivity : AppCompatActivity() {
lateinit var binding:ActivityOnboardingBinding
    private lateinit var imageViewPager: ViewPager2
    private lateinit var textViewPager: ViewPager2
    private lateinit var indicator: CircleIndicator3
    private lateinit var buttonNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageViewPager = findViewById(R.id.imageViewPager)
        textViewPager = findViewById(R.id.textViewPager)
        indicator = findViewById(R.id.indicator)
        buttonNext = findViewById(R.id.buttonNext)

        val items = listOf(
            OnBoardingItem(
                R.drawable.farmer1,
                "Smart Farming Made Easy",
                "Monitor your farm's health with real-time data on soil and climate conditions."
            ),
            OnBoardingItem(
                R.drawable.farmer2,
                "Optimize Water & Soil Quality",
                "Track soil moisture, humidity, and pH levels to ensure the best growing conditions."
            ),
            OnBoardingItem(
                R.drawable.farmer3,
                "Stay Ahead with Smart Insights",
                "Get accurate temperature and environmental updates to make informed farming decisions."
            )
        )

        val imageAdapter = ImagePagerAdapter(items.map { it.image })
        val textAdapter = TextPagerAdapter(items)

        imageViewPager.adapter = imageAdapter
        textViewPager.adapter = textAdapter

        // Synchronize ViewPager scrolling
        imageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                textViewPager.currentItem = position
            }
        })

        textViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                imageViewPager.currentItem = position
            }
        })

        // Attach CircleIndicator3 with textViewPager
        indicator.setViewPager(textViewPager)

        buttonNext.setOnClickListener {
            val bottomSheet = LoginBottomSheet()
            bottomSheet.show(supportFragmentManager, "LoginBottomSheet")
            //Toast.makeText(applicationContext, "Clicked", Toast.LENGTH_SHORT).show()
        }

    }
}
