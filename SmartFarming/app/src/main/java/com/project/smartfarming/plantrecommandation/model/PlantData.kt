package com.project.smartfarming.plantrecommandation.model

data class PlantData(
    val description: String,
    val id: String,
    val image: String,
    val max_ambience_light: String,
    val max_ph_value: String,
    val max_soil_moisture: String,
    val max_temperature: String,
    val min_ambience_light: String,
    val min_ph_value: String,
    val min_soil_moisture: String,
    val min_temperature: String,
    val title: String
)