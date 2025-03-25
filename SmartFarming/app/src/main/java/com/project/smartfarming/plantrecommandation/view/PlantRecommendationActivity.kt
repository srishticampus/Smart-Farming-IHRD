package com.project.smartfarming.plantrecommandation.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.smartfarming.R
import com.project.smartfarming.api.ApiUtilities
import com.project.smartfarming.databinding.ActivityPlantRecommendationBinding
import com.project.smartfarming.plantrecommandation.model.Plant
import com.project.smartfarming.plantrecommandation.model.PlantResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlantRecommendationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlantAdapter
    private lateinit var binding: ActivityPlantRecommendationBinding
    private var plantList: MutableList<Plant> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlantRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initialize adapter with an empty list
        adapter = PlantAdapter(plantList) { selectedPlant ->
            val intent = Intent(this, PlantDescriptionActivity::class.java).apply {
                putExtra("plantName", selectedPlant.name)
                putExtra("plantImage", selectedPlant.imageRes)
                putExtra("plantDescription", selectedPlant.description)
            }
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Fetch data from API
        fetchPlantData()

        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchPlantData() {
        //binding.progressBar.visibility = View.VISIBLE // Show loading indicator

        ApiUtilities.getInstance().getPlantSuggestions().enqueue(object : Callback<PlantResponse> {
            override fun onResponse(call: Call<PlantResponse>, response: Response<PlantResponse>) {

                if (response.isSuccessful && response.body() != null) {
                    val plants = response.body()?.data ?: emptyList()
                    plantList.clear()
                    plantList.addAll(plants.map {
                        Plant(it.title, it.image, it.description)
                    })
                    adapter.notifyDataSetChanged() // Refresh RecyclerView
                } else {
                    Toast.makeText(
                        this@PlantRecommendationActivity,
                        "Failed to fetch plants",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                Toast.makeText(
                    this@PlantRecommendationActivity,
                    "API Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("PlantAPI", "Error: ${t.message}")
            }
        })
    }
}
