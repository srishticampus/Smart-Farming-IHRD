package com.project.smartfarming.signup.model

data class SignupResponse(
    val message: String,
    val status: Boolean,
    val userData: List<SignupData>
)