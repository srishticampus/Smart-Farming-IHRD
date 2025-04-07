package com.project.agritech.editprofile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.project.agritech.R
import com.project.agritech.api.ApiUtilities
import com.project.agritech.databinding.ActivityEditProfileBinding
import com.project.agritech.utlis.SharedPreferencesManager
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
        // binding.updateButton.setOnClickListener { updateProfile(userId) }
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
        if (!validateFields()) return
// Required userId field
        val userIdRequest = userId.toRequestBody("text/plain".toMediaTypeOrNull())

        // Send the original values if they are not changed
        val nameRequest = currentName.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailRequest = currentEmail.toRequestBody("text/plain".toMediaTypeOrNull())
        val phoneRequest = currentPhone.toRequestBody("text/plain".toMediaTypeOrNull())

        // Address can be null if not changed
        val addressRequest = if (currentAddress.isNotEmpty())
            currentAddress.toRequestBody("text/plain".toMediaTypeOrNull())
        else null

        // Handle profile image only if changed
        var imagePart: MultipartBody.Part? = null
        if (imageUri != null) {
            val file = getFileFromUri(imageUri)
            if (file != null && file.exists()) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                imagePart =
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
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
                    userid = userIdRequest,
                    name = nameRequest,
                    email = emailRequest,
                    phone = phoneRequest,
                    address = addressRequest,
                    image = imagePart
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

        binding.usernameField.setText(name)
        binding.emailField.setText(email)
        binding.phoneField.setText(phone)
        binding.addressField.setText(address)
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

    private fun validateFields(): Boolean {
        var isValid = true

        // Reset errors
        binding.usernameField.error = null
        binding.emailField.error = null
        binding.phoneField.error = null
        binding.addressField.error = null

        // Name Validation
        val name = binding.usernameField.text.toString().trim()
        val namePattern = "^[A-Za-z ]+$"
        if (name.isEmpty()) {
            binding.usernameField.error = "Name is required"
            isValid = false
        } else if (name.length < 2 || name.length > 20) {
            binding.usernameField.error = "Name must be between 2 and 20 characters"
            isValid = false
        } else if (!name.matches(namePattern.toRegex())) {
            binding.usernameField.error =
                "Invalid name. Only alphabetic characters and spaces are allowed"
            isValid = false
        }

        // Phone Validation
        val phone = binding.phoneField.text.toString().trim()
        val phonePattern = "^[0-9]{10}$"
        if (phone.isEmpty()) {
            binding.phoneField.error = "Phone number is required"
            isValid = false
        } else if (!phone.matches(phonePattern.toRegex())) {
            binding.phoneField.error = "Enter a valid 10-digit phone number"
            isValid = false
        }

        // Email Validation
        val email = binding.emailField.text.toString().trim()
        if (email.isEmpty()) {
            binding.emailField.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailField.error = "Invalid email address"
            isValid = false
        }

        // Address Validation
        val address = binding.addressField.text.toString().trim()
        if (address.isEmpty()) {
            binding.addressField.error = "Address is required"
            isValid = false
        } else if (address.length < 5) {
            binding.addressField.error = "Address must be at least 5 characters long"
            isValid = false
        }

        // Profile Image Validation (Only if a new image is selected)
        if (imageUri != null) {
            val file = getFileFromUri(imageUri!!)
            if (file == null || !file.exists() || file.length() == 0L) {
                showToast("Invalid profile image selected")
                isValid = false
            }
        }

        if (!isValid) {
            showToast("Please correct the errors above")
        }

        return isValid
    }
}
