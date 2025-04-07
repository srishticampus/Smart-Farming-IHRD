package com.project.smartfarming.plantrecommandation.model

data class PlantResponse(
    val data: List<PlantData>,
    val message: String,
    val status: Boolean
)