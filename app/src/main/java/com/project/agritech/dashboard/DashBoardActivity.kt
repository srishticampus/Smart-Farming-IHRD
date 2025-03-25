package com.project.agritech.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.agritech.R
import com.project.agritech.api.ApiUtilities
import com.project.agritech.databinding.ActivityDashBoardBinding
import com.project.agritech.plantrecommandation.model.PlantItem
import com.project.agritech.plantrecommandation.model.PlantResponse
import com.project.agritech.plantrecommandation.view.PlantRecommendationActivity
import com.project.agritech.profile.ViewProfileActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import java.net.HttpURLConnection
import java.net.URL

class DashBoardActivity : AppCompatActivity() {
    lateinit var binding: ActivityDashBoardBinding
    private lateinit var database: DatabaseReference
    private var previousWaterLevel: Float = 0f
    private var waterUsage: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.settings.setOnClickListener {
            val intent = Intent(applicationContext, ViewProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        val viewPlantsButton: Button = findViewById(R.id.viewPlantsButton)
        viewPlantsButton.setOnClickListener {
//            val intent = Intent(this, PlantRecommendationActivity::class.java)
//            startActivity(intent)
            fetchPlantSuggestions()
        }

        initializeDatabaseListener()

    }

    private fun fetchPlantSuggestions() {
        ApiUtilities.getInstance().getPlantSuggestions()
            .enqueue(object : retrofit2.Callback<PlantResponse> {
                override fun onResponse(
                    call: Call<PlantResponse>,
                    response: retrofit2.Response<PlantResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val plantList = response.body()?.data ?: emptyList()

                        // Only send image & title
                        val intent =
                            Intent(this@DashBoardActivity, PlantRecommendationActivity::class.java)
                        intent.putParcelableArrayListExtra("plantList", ArrayList(plantList.map {
                            PlantItem(it.image, it.title)
                        }))
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Failed to fetch plants",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                    Toast.makeText(
                        applicationContext,
                        "API Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun initializeDatabaseListener() {
        database = FirebaseDatabase.getInstance().reference

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val phValue =
                        snapshot.child("Sensor/phLevel").value.toString().toFloatOrNull() ?: 7f
                    val tankLevelRaw =
                        snapshot.child("Sensor/distance").value.toString().toFloatOrNull() ?: 0f
                    val humidity = snapshot.child("Sensor/humidity").value.toString()
                    val lightRaw =
                        snapshot.child("Sensor/light").value.toString().toFloatOrNull() ?: 0f
                    val soilMoistureRaw =
                        snapshot.child("Sensor/soilMoisture").value.toString().toFloatOrNull() ?: 0f
                    val temperature =
                        snapshot.child("Sensor/temperature").value.toString().toFloatOrNull() ?: 0f

                    val lightPercentage = lightRaw.toInt().coerceIn(0, 100)
                    val phProgress = phValue.toInt().coerceIn(0, 14)
                    val soilMoisturePercentage = soilMoistureRaw.toInt().coerceIn(0, 100)
                    binding.tempValue.text = "$temperature°C"
                    binding.perceantgeOfLight.text = "$lightPercentage%"
                    binding.moisturePercentage.text = "$soilMoisturePercentage%"
                    binding.phValueText.text = "${phValue}ph"

                    binding.tankPercentage.text = "$tankLevelRaw%"
                    binding.tankProgressBar.progress = tankLevelRaw.toInt()

                    binding.phProgressBar.progress = phProgress
                    binding.progressBar.progress = lightPercentage
                    binding.gaugeView.updateMoistureLevel(soilMoisturePercentage)

                    sendSensorDataToServer(
                        phValue,
                        temperature,
                        lightPercentage,
                        soilMoisturePercentage,
                        tankLevelRaw
                    )

                    if (tankLevelRaw < previousWaterLevel) {
                        val waterUsed = previousWaterLevel - tankLevelRaw
                        Log.d(
                            "WaterUsage",
                            "Water Used: $waterUsed (Previous: $previousWaterLevel, Current: $tankLevelRaw)"
                        )
                        waterUsage += waterUsed
                        sendWaterUsageToServer(waterUsed)
                    } else {
                        Log.d(
                            "WaterUsage",
                            "No water usage detected (Previous: $previousWaterLevel, Current: $tankLevelRaw)"
                        )
                    }
                    previousWaterLevel = tankLevelRaw


                    when (soilMoisturePercentage) {
                        in 0..30 -> {
                            binding.statusText.text = "Wet"
                            binding.statusText.setTextColor(
                                ContextCompat.getColor(
                                    this@DashBoardActivity,
                                    android.R.color.holo_blue_dark
                                )
                            )
                        }

                        in 31..70 -> {
                            binding.statusText.text = "Optimal"
                            binding.statusText.setTextColor(
                                ContextCompat.getColor(
                                    this@DashBoardActivity,
                                    android.R.color.holo_green_dark
                                )
                            )
                        }

                        in 71..100 -> {
                            binding.statusText.text = "Dry"
                            binding.statusText.setTextColor(
                                ContextCompat.getColor(
                                    this@DashBoardActivity,
                                    android.R.color.holo_red_dark
                                )
                            )
                        }
                    }

                    val normalColor = Color.parseColor("#666666") // Default gray
                    val highlightColor = ContextCompat.getColor(
                        applicationContext,
                        R.color.btnClr
                    )
                    binding.coldLabel.setTextColor(normalColor)
                    binding.optimalLabel.setTextColor(normalColor)
                    binding.hotLabel.setTextColor(normalColor)

                    binding.coldTemp.setTextColor(normalColor)
                    binding.optimalTemp.setTextColor(normalColor)
                    binding.hotTemp.setTextColor(normalColor)

                    when {
                        temperature < 15 -> {
                            binding.coldLabel.setTextColor(highlightColor)
                            binding.coldTemp.setTextColor(highlightColor)
                        }

                        temperature in 15.0..30.0 -> {
                            binding.optimalLabel.setTextColor(highlightColor)
                            binding.optimalTemp.setTextColor(highlightColor)
                        }

                        temperature > 30 -> {
                            binding.hotLabel.setTextColor(highlightColor)
                            binding.hotTemp.setTextColor(highlightColor)
                        }
                    }

                } catch (e: Exception) {
                    Log.e("FirebaseError", "Error fetching data: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    applicationContext,
                    "Failed to read sensor data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun sendWaterUsageToServer(waterUsed: Float) {
        if (waterUsed <= 0) {
            Log.d("WaterUsage", "No significant water usage detected: $waterUsed")
            return // Don't send if no water was used
        }

        val jsonBody = JSONObject().apply {
            put("water_usage", waterUsed)
        }.toString()

        Log.d("WaterUsage", "Sending data: $jsonBody") // Log data before sending

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://campus.sicsglobal.co.in/Project/Agritech/api/water_usage.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST" // Ensure it's POST
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.doInput = true

                // Send JSON data
                connection.outputStream.use { outputStream ->
                    outputStream.write(jsonBody.toByteArray())
                    outputStream.flush() // Ensure data is written completely
                }

                // Read server response
                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("ServerResponse", "Water usage sent successfully: $responseMessage")
                } else {
                    Log.e(
                        "ServerResponse",
                        "Failed to send water usage: $responseCode, $responseMessage"
                    )
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("HttpError", "Error sending water usage: ${e.message}")
            }
        }
    }


    private fun sendSensorDataToServer(
        phValue: Float,
        temperature: Float,
        ambientLight: Int,
        soilMoisture: Int,
        waterLevel: Float
    ) {
        val jsonBody = JSONObject().apply {
            put("ph_value", phValue)
            put("temperature", temperature)
            put("ambience_light", ambientLight)
            put("soil_moisture", soilMoisture)
            put("water_level", waterLevel)
        }.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url =
                    URL("http://campus.sicsglobal.co.in/Project/Agritech/api/store_sensor_data.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                connection.outputStream.use { outputStream ->
                    outputStream.write(jsonBody.toByteArray())
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("ServerResponse", "Sensor data sent successfully")
                } else {
                    Log.e("ServerResponse", "Failed to send data: $responseCode")
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e("HttpError", "Error sending data to PHP server: ${e.message}")
            }
        }
    }
}