package com.project.smartfarming.editprofile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.project.smartfarming.R
import com.project.smartfarming.api.ApiUtilities
import com.project.smartfarming.databinding.ActivityEditProfileBinding
import com.project.smartfarming.utlis.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class EditProfileActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_PICK = 101
    private var imageUri: Uri? = null
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private var originalName = ""
    private var originalEmail = ""
    private var originalPhone = ""
    private var originalAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesManager = SharedPreferencesManager(this)
        val userId = sharedPreferencesManager.getUserId()

        viewProfile(userId)
        binding.backButton.setOnClickListener { finish() }
        binding.profileImage.setOnClickListener { openGalleryForImageSelection() }
        binding.updateButton.setOnClickListener { updateProfile(userId, imageUri) }
    }

    private fun viewProfile(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = ApiUtilities.getInstance()
                val result = api.getUserProfile(userId)

                withContext(Dispatchers.Main) {
                    result.body()?.let { root ->
                        if (root.status == true && root.userData.isNotEmpty()) {
                            val userData = root.userData[0]
                            binding.usernameField.setText(userData.name)
                            binding.emailField.setText(userData.email)
                            binding.phoneField.setText(userData.phone)
                            binding.addressField.setText(userData.address)

                            if (!userData.photo.isNullOrEmpty()) {
                                Glide.with(this@EditProfileActivity)
                                    .load(userData.photo)
                                    .placeholder(R.drawable.profile_icon)
                                    .error(R.drawable.error_image)
                                    .into(binding.profileImage)
                            }
                            storeOriginalData(
                                userData.name,
                                userData.email,
                                userData.phone,
                                userData.address
                            )
                        } else {
                            showToast(root.message ?: "Profile loading failed")
                        }
                    } ?: showToast("Server Error: ${result.message()}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun updateProfile(userId: String, imageUri: Uri?) {
        val currentName = binding.usernameField.text.toString().trim()
        val currentEmail = binding.emailField.text.toString().trim()
        val currentPhone = binding.phoneField.text.toString().trim()
        val currentAddress = binding.addressField.text.toString().trim()

        // Validate inputs
        if (!validateFields(currentName, currentEmail, currentPhone, currentAddress)) return

        // Required userId field
        val userIdRequest = userId.toRequestBody("text/plain".toMediaTypeOrNull())

        // Send only changed fields
        val nameRequest = if (currentName != originalName)
            currentName.toRequestBody("text/plain".toMediaTypeOrNull()) else null

        val emailRequest = if (currentEmail != originalEmail)
            currentEmail.toRequestBody("text/plain".toMediaTypeOrNull()) else null

        val phoneRequest = if (currentPhone.isNotEmpty() && currentPhone != originalPhone)
            currentPhone.toRequestBody("text/plain".toMediaTypeOrNull()) else null

        val addressRequest = if (currentAddress != originalAddress)
            currentAddress.toRequestBody("text/plain".toMediaTypeOrNull()) else null

        // Handle profile image only if changed
        var imagePart: MultipartBody.Part? = null
        if (imageUri != null) {
            val file = getFileFromUri(imageUri)
            if (file != null && file.exists()) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                imagePart =
                    MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
            }
        }

        // **Fix Logging Issue**: Show actual RequestBody values
        Log.d(
            "UpdateProfile", "Sending Updated Data: ID: $userId, " +
                    "Name: ${nameRequest?.stringValue()}, " +
                    "Email: ${emailRequest?.stringValue()}, " +
                    "Phone: ${phoneRequest?.stringValue()}, " +
                    "Address: ${addressRequest?.stringValue()}"
        )

        Log.d("UpdateProfile", "Image Part: ${imagePart?.body?.contentLength()} bytes")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiUtilities.getInstance().updateProfile(
                    id = userIdRequest,
                    name = nameRequest,
                    email = emailRequest,
                    phone = phoneRequest,
                    address = addressRequest,
                    profileImage = imagePart
                )

                withContext(Dispatchers.Main) {
                    Log.d("UpdateProfile", "Raw Response: ${response.raw()}")
                    Log.d(
                        "UpdateProfile",
                        "Response Body: ${response.body()?.toString() ?: "Null"}"
                    )

                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result != null && result.status) {
                            showToast("Profile updated successfully")

                            // Update only changed fields in SharedPreferences
                            if (nameRequest != null) sharedPreferencesManager.saveUsername(
                                currentName
                            )
                            if (emailRequest != null) sharedPreferencesManager.saveEmail(
                                currentEmail
                            )
                            if (phoneRequest != null) sharedPreferencesManager.savePhoneNumber(
                                currentPhone
                            )
                            if (addressRequest != null) sharedPreferencesManager.saveAddress(
                                currentAddress
                            )
                            if (imageUri != null) sharedPreferencesManager.saveProfileImage(imageUri.toString())

                        } else {
                            showToast(result?.message ?: "Update failed")
                        }
                    } else {
                        showToast("Server error: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("An error occurred: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun storeOriginalData(name: String, email: String, phone: String, address: String) {
        originalName = name
        originalEmail = email
        originalPhone = phone
        originalAddress = address
    }

    private fun openGalleryForImageSelection() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.profileImage.setImageURI(imageUri)
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "temp_image.jpg")
            file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
            if (file.exists() && file.length() > 0) file else null
        } catch (e: Exception) {
            Log.e("UpdateProfile", "File conversion failed: ${e.localizedMessage}")
            null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun RequestBody.stringValue(): String {
        return try {
            val buffer = okio.Buffer()
            this.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: Exception) {
            "Error reading RequestBody"
        }
    }

    private fun validateFields(
        name: String,
        email: String,
        phone: String,
        address: String
    ): Boolean {
        if (name.isEmpty() && email.isEmpty() && phone.isEmpty() && address.isEmpty() && imageUri == null) {
            showToast("No changes detected")
            return false
        }
        if (phone.isNotEmpty() && phone.length != 10) {
            showToast("Enter a valid 10-digit phone number")
            return false
        }
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Enter a valid email address")
            return false
        }
        if (imageUri != null) {
            val file = getFileFromUri(imageUri!!)
            if (file == null || !file.exists() || file.length() == 0L) {
                showToast("Invalid profile image selected")
                return false
            }
        }
        return true
    }
}
