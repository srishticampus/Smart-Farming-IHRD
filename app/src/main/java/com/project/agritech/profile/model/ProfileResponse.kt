package com.project.agritech.profile.model

data class ProfileResponse(
    val message: String,
    val status: Boolean,
    val userData: List<ProfileData>
)