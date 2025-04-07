package com.project.agritech.utlis

import android.content.Context
import android.content.SharedPreferences


class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "userId"
        private const val KEY_PHONE_NUMBER = "phone"
        private const val LOGIN_STATUS = "loginStatus"
        private const val KEY_USERNAME = "username"
        private const val KEY_ADDRESS = "address"
        private const val KEY_EMAILS = "emails" // Added for emails
        private const val KEY_PROFILE_IMAGE = "profile_image"

    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun savePhoneNumber(phone: String) {
        sharedPreferences.edit().putString(KEY_PHONE_NUMBER, phone).apply()
    }

    fun saveEmail(email: String) {
        sharedPreferences.edit().putString(KEY_EMAILS, email).apply()
    }

    fun getEmail(): String {
        return sharedPreferences.getString(KEY_EMAILS, "") ?: ""
    }

    fun getUserId(): String {
        return sharedPreferences.getString(KEY_USER_ID, "") ?: ""
    }

    fun saveLoginStatus(loginStatus: String) {
        sharedPreferences.edit().putString(LOGIN_STATUS, loginStatus).apply()
    }

    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String {
        return sharedPreferences.getString(KEY_USERNAME, "") ?: ""
    }

    fun saveAddress(address: String) {
        sharedPreferences.edit().putString(KEY_ADDRESS, address).apply()
    }

    fun getAddress(): String {
        return sharedPreferences.getString(KEY_ADDRESS, "") ?: ""
    }

    fun saveProfileImage(imageUrl: String) {
        sharedPreferences.edit().putString(KEY_PROFILE_IMAGE, imageUrl).apply()
    }

    fun getProfileImage(): String {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE, "") ?: ""
    }

    fun getPhoneNumber(): String {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, "") ?: ""
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getString(LOGIN_STATUS, "false") == "true"
    }

    fun saveProfileData(name: String, email: String, phone: String, address: String, profileImage: String?) {
        saveUsername(name)
        saveEmail(email)
        savePhoneNumber(phone)
        saveAddress(address)
        profileImage?.let { saveProfileImage(it) }
    }

}