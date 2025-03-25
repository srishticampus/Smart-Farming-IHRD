package com.project.smartfarming.waterusage.model

data class DateResponse(
    val data: List<Data>,
    val message: String,
    val status: Boolean
)