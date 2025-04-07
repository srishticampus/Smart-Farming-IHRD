package com.project.agritech.signup.model

data class SignupResponse(
    val message: String,
    val status: Boolean,
    val userData: List<SignupData>
)