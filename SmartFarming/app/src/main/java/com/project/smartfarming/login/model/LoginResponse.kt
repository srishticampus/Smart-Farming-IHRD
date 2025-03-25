package com.project.smartfarming.login.model

data class LoginResponse(
    val message: String,
    val status: Boolean,
    val userData: List<UserData>
)