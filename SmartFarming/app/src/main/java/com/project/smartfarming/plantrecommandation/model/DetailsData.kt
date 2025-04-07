package com.project.smartfarming.plantrecommandation.model

data class DetailsData(
    val description: String,
    val id: Int,
    val image: String,
    val max_ambience_light: Int,
    val max_ph_value: String,
    val max_soil_moisture: Int,
    val max_temperature: String,
    val min_ambience_light: Int,
    val min_ph_value: String,
    val min_soil_moisture: Int,
    val min_temperature: String,
    val title: String
)