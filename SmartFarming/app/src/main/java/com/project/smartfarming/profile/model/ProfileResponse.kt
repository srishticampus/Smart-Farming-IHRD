package com.project.smartfarming.profile.model

data class ProfileResponse(
    val message: String,
    val status: Boolean,
    val userData: List<ProfileData>
)