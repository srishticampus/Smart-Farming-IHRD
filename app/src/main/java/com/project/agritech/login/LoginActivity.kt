package com.project.agritech.login

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.project.agritech.R
import com.project.agritech.api.ApiUtilities
import com.project.agritech.dashboard.DashBoardActivity
import com.project.agritech.databinding.ActivityLoginBinding
import com.project.agritech.signup.SignupActivity
import com.project.agritech.utlis.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreference: SharedPreferencesManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        sharedPreference = SharedPreferencesManager(applicationContext)
        setContentView(binding.root)

        binding.hidePassword.setOnClickListener(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.forgotTv.setOnClickListener {
            val intent = Intent(applicationContext, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginSubmit.setOnClickListener {

            val phone = binding.userName.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()
            if (checkAllFields()) {
                val params = HashMap<String?, String>()
                params["phone"] = phone
                params["password"] = password

                Log.d("LoginDebug", "Entered Phone: $phone")
                Log.d("LoginDebug", "Entered Password: $password")

                lifecycleScope.launch(Dispatchers.IO) {
                    val response = ApiUtilities.getInstance().userLogin(params)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            if (result?.status == true) {
                                val userData = result.userData[0]

                                sharedPreference.saveUserId(userData.id)
                                sharedPreference.savePhoneNumber(userData.phone)
                                sharedPreference.saveLoginStatus("true")

                                val intent =
                                    Intent(this@LoginActivity, DashBoardActivity::class.java)
                                startActivity(intent)
                                finish()
                                Toast.makeText(
                                    applicationContext,
                                    "User Logged in successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    result?.message ?: "Login failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } else {
                            Toast.makeText(
                                applicationContext,
                                response.message(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(applicationContext, "Enter the details", Toast.LENGTH_SHORT).show()
            }
        }
        binding.signupTextBtn.setOnClickListener {
            val intent = Intent(applicationContext, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkAllFields(): Boolean {
        var isValid = true
        val userName = binding.userName.text.toString().trim()
        val password = binding.loginPassword.text.toString()

        // Username validation
        if (userName.isEmpty()) {
            binding.userName.error = "Number is required"
            isValid = false
        }

        // Password validation
        if (password.isEmpty()) {
            binding.loginPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 4) {
            binding.loginPassword.error = "Password must be at least 4 characters"
            isValid = false
        } else if (password != password.trim()) {
            binding.loginPassword.error = "Password should not contain spaces at start or end"
            isValid = false
        }

        return isValid
    }
    override fun onClick(v: View?) {
        if (v?.id == R.id.hidePassword) {
            if (binding.loginPassword.transformationMethod
                    .equals(PasswordTransformationMethod.getInstance())
            ) {
                binding.hidePassword.setImageResource(R.drawable.ic_hide_eyes)
                binding.loginPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.hidePassword.setImageResource(R.drawable.ic_password_eye)
                binding.loginPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }
    }
}