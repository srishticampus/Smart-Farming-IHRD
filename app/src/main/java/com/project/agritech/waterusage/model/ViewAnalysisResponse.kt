package com.project.agritech.waterusage.model

data class ViewAnalysisResponse(
    val date: String,
    val message: String,
    val status: Boolean,
    val total_water_level: String,
    val total_water_usage: String
)