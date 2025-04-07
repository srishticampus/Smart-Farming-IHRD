package com.project.agritech.waterusage

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.project.agritech.R
import com.project.agritech.api.ApiUtilities
import com.project.agritech.databinding.ActivityAnalysisBinding
import com.project.agritech.waterusage.model.Data
import com.project.agritech.waterusage.model.ViewAnalysisResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalysisActivity : AppCompatActivity() {
    lateinit var binding: ActivityAnalysisBinding
    private var dateList = mutableListOf<String>()
    private lateinit var barChart: BarChart


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        fetchWaterUsageDates()

        binding.spinnerDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedDate = dateList[position]
                fetchWaterUsageAnalysis(selectedDate)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


    }

    private fun fetchWaterUsageDates() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiUtilities.getInstance().getWaterUsageDates()
                if (response.isSuccessful && response.body() != null) {
                    val dates = response.body()?.data ?: emptyList()
                    updateSpinner(dates)
                } else {
                    showToast("Failed to load dates")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching dates: ${e.message}")
                showToast("Error fetching dates")
            }
        }
    }

    private suspend fun updateSpinner(dates: List<Data>) {
        withContext(Dispatchers.Main) {
            dateList.clear()
            dateList.addAll(dates.map { it.date })

            val adapter = ArrayAdapter(
                this@AnalysisActivity,
                android.R.layout.simple_spinner_item,
                dateList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDate.adapter = adapter
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@AnalysisActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchWaterUsageAnalysis(selectedDate: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiUtilities.getInstance().getWaterUsageAnalysis(selectedDate)
                if (response.isSuccessful && response.body() != null) {
                    val analysisData = response.body()!!
                    updateBarChart(analysisData)
                } else {
                    showToast("Failed to fetch analysis data")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching analysis: ${e.message}")
                showToast("Error fetching analysis")
            }
        }
    }

    private suspend fun updateBarChart(analysisData: ViewAnalysisResponse) {
        withContext(Dispatchers.Main) {
            val entries = mutableListOf<BarEntry>()

            try {
                val totalUsage = analysisData.total_water_usage.toFloat()
                val totalLevel = analysisData.total_water_level.toFloat()

                entries.add(BarEntry(1f, totalUsage))
                entries.add(BarEntry(2f, totalLevel))

                val dataSet = BarDataSet(entries, "Water Usage Analysis")
                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

                val barData = BarData(dataSet)
                barData.barWidth = 0.6f

                barChart.data = barData
                barChart.description.isEnabled = false
                barChart.animateY(1000)

                val xAxis = barChart.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textColor = Color.BLACK
                xAxis.labelCount = entries.size

                barChart.invalidate() // Refresh chart
            } catch (e: Exception) {
                Log.e("CHART_ERROR", "Error updating chart: ${e.message}")
            }
        }
    }
}