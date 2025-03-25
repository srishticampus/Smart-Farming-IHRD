package com.project.agritech.plantrecommandation.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.agritech.R
import com.project.agritech.databinding.ActivityPlantDescriptionBinding

class PlantDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlantDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val plantName = intent.getStringExtra("plantName")
        val plantImage = intent.getIntExtra("plantImage", 0)
        val plantDescription = intent.getStringExtra("plantDescription")

        binding.plantTitle.text = plantName
        binding.plantDesc.text = plantDescription
        binding.plantImageView.setImageResource(plantImage)

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}