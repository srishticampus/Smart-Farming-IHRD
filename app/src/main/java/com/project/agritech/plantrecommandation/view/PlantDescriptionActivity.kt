package com.project.agritech.plantrecommandation.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.project.agritech.R
import com.project.agritech.databinding.ActivityPlantDescriptionBinding

class PlantDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlantDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle window insets (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Get plant data from intent
        val plantName = intent.getStringExtra("plantName")
        val plantImageUrl = intent.getStringExtra("plantImage") // ✅ This is now a URL
        val plantDescription = intent.getStringExtra("plantDescription")

        // ✅ Set data to views
        binding.plantTitle.text = plantName
        binding.plantDesc.text = plantDescription

        // ✅ Load image from URL using Glide
        Glide.with(this)
            .load(plantImageUrl)
            .placeholder(R.drawable.tomato)
            .error(R.drawable.fig)
            .into(binding.plantImageView)

        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
