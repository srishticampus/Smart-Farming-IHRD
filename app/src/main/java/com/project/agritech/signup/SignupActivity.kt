package com.project.agritech.signup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.project.agritech.R
import com.project.agritech.api.ApiUtilities
import com.project.agritech.databinding.ActivitySignupBinding
import com.project.agritech.login.LoginActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SignupActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivitySignupBinding

    private lateinit var imgProfile: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imgProfile = findViewById(R.id.imgProfile)

        imgProfile.setOnClickListener {
            openImagePicker()
        }
        binding.hidePassword.setOnClickListener(this)
        binding.hideConfirmPassword.setOnClickListener(this)

        binding.loginTextBtn.setOnClickListener {
            intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.signupButton.setOnClickListener {
            if (checkAllFields()) {
                registerUser()
            }
        }

        binding.password.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.toString().contains(" ")) "" else null
        })

        binding.confirmPassword.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.toString().contains(" ")) "" else null
        })
    }

    private fun registerUser() {
        val username = binding.userName.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val phone = binding.phoneNumber.text.toString().trim()
        val address = binding.address.text.toString().trim()
        val password = binding.password.text.toString().trim()

        // Create RequestBody for text fields
        val nameBody = createPartFromString(username)
        val emailBody = createPartFromString(email)
        val phoneBody = createPartFromString(phone)
        val addressBody = createPartFromString(address)
        val passwordBody = createPartFromString(password)

        // Create MultipartBody.Part for the image if available
        var imagePart: MultipartBody.Part? = null

        if (selectedImageUri != null) {
            val imageFile = getFileFromUri(selectedImageUri!!)
            if (imageFile != null) {
                val requestFile = RequestBody.create("image/*".toMediaType(), imageFile)
                imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            }
        }

        // Call API using Retrofit
        lifecycleScope.launch {
            try {
                val response = ApiUtilities.getInstance().registerUser(
                    nameBody, emailBody, phoneBody, addressBody, passwordBody, imagePart
                )

                if (response.isSuccessful && response.body() != null) {
                    val signupResponse = response.body()!!
                    if (signupResponse.status == true) { // Ensure response contains success status
                        Toast.makeText(
                            this@SignupActivity,
                            "Success: ${signupResponse.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate to LoginActivity only on successful signup
                        startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        // Show error message from API response
                        Toast.makeText(
                            this@SignupActivity,
                            "Signup Failed: ${signupResponse.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // Handle API error (e.g., duplicate email/phone)
                    val errorMessage = response.errorBody()?.string() ?: "Signup failed. Try again."
                    Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SignupActivity, "Exception: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    // Helper function to create RequestBody from String
    private fun createPartFromString(value: String): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), value)
    }

    private fun openImagePicker() {
      openGallery()
    }
    private fun getFileFromUri(uri: Uri): File? {
        val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        val tempFile = File(cacheDir, fileName)

        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                imgProfile.setImageURI(selectedImageUri)
            }
        }

    private fun checkAllFields(): Boolean {
        var isValid = true

        // Reset errors
        binding.userName.error = null
        binding.email.error = null
        binding.phoneNumber.error = null
        binding.address.error = null
        binding.password.error = null
        binding.confirmPassword.error = null

        val namePattern = "^[A-Za-z]+( [A-Za-z]+)*$"

        // Full Name Validation
        val fullName = binding.userName.text.toString().trim()
        if (fullName.isEmpty()) {
            binding.userName.error = "Full name is required"
            isValid = false
        } else if (!fullName.matches(namePattern.toRegex())) {
            binding.userName.error = "Only alphabetic characters allowed"
            isValid = false
        }

        // Email Validation
        val email = binding.email.text.toString().trim()
        if (email.isEmpty()) {
            binding.email.error = "Email is required"
            isValid = false
        } else if (!isEmailValid(email)) {
            binding.email.error = "Invalid email address"
            isValid = false
        }

        // Phone Number Validation
        val phone = binding.phoneNumber.text.toString().trim()
        val phonePattern = "^[+]?[0-9]{10,12}$"
        if (phone.isEmpty()) {
            binding.phoneNumber.error = "Phone number is required"
            isValid = false
        } else if (!phone.matches(phonePattern.toRegex())) {
            binding.phoneNumber.error = "Enter a valid phone number"
            isValid = false
        }

        // Address Validation
        val address = binding.address.text.toString().trim()
        if (address.isEmpty()) {
            binding.address.error = "Address is required"
            isValid = false
        }

        // Password Validation
        val password = binding.password.text.toString()
        val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#\$%^&+=!])(?!.*\\s).{6,8}$"
        if (password.isEmpty()) {
            binding.password.error = "Password is required"
            isValid = false
        } else if (!password.matches(passwordPattern.toRegex())) {
            binding.password.error = "Include 1 uppercase, 1 number, 1 special character"
            isValid = false
        }

        // Confirm Password Validation
        val confirmPassword = binding.confirmPassword.text.toString()
        if (confirmPassword.isEmpty()) {
            binding.confirmPassword.error = "Confirm Password is required"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    // Helper function to validate email
    private fun isEmailValid(email: String): Boolean {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        val domainPart = email.substringAfterLast(".", "")
        return domainPart.length >= 2 // Ensure TLD is at least 2 characters long
    }

    private fun showHidePassWord(v: View?) {
        if (v?.id == R.id.hidePassword) {
            if (binding.password.transformationMethod
                    .equals(PasswordTransformationMethod.getInstance())
            ) {
                binding.hidePassword.setImageResource(R.drawable.ic_hide_eyes)
                binding.password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.hidePassword.setImageResource(R.drawable.ic_password_eye)
                binding.password.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }
    }

    private fun hideConfirmPassword(v: View?) {
        if (v?.id == R.id.hideConfirmPassword) {
            if (binding.confirmPassword.transformationMethod
                    .equals(PasswordTransformationMethod.getInstance())
            ) {
                binding.hideConfirmPassword.setImageResource(R.drawable.ic_hide_eyes)
                binding.confirmPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.hideConfirmPassword.setImageResource(R.drawable.ic_password_eye)
                binding.confirmPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.hidePassword -> {
                showHidePassWord(v)
            }

            R.id.hideConfirmPassword -> {
                hideConfirmPassword(v)
            }
        }
    }
}