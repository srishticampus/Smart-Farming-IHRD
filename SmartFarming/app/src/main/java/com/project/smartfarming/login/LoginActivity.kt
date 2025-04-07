package com.project.smartfarming.login

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.project.smartfarming.R
import com.project.smartfarming.api.ApiUtilities
import com.project.smartfarming.dashboard.DashBoardActivity
import com.project.smartfarming.databinding.ActivityLoginBinding
import com.project.smartfarming.signup.SignupActivity
import com.project.smartfarming.utlis.SharedPreferencesManager
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
            val params = HashMap<String?, String>()
            params["phone"] = binding.userName.text.toString()
            params["password"] = binding.loginPassword.text.toString()

            if (checkAllFields()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val response = ApiUtilities.getInstance().userLogin(params)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            if (result?.status == true) {
                                result.userData.get(0).let {
                                    sharedPreference.saveUserId(it.id)
                                    sharedPreference.savePhoneNumber(it.phone)
                                    sharedPreference.saveLoginStatus("true") // Save login status
                                }
                                val intent =
                                    Intent(this@LoginActivity, DashBoardActivity::class.java)
                                startActivity(intent)
                                finish() // Close login activity
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
        if (binding.userName.length() == 0) {
            binding.userName.error = "Number is required"
            isValid = false
        }
        if (binding.loginPassword.length() == 0) {
            binding.loginPassword.error = "Password is required"
            isValid = false
        } else if (binding.loginPassword.length() < 4) {
            binding.loginPassword.error = "Password must be at least 4 characters"
            isValid = false
        }
        return isValid
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.hidePassword) {
            if (binding.loginPassword.transformationMethod == PasswordTransformationMethod.getInstance()) {
                binding.hidePassword.setImageResource(R.drawable.ic_password_eye)
                binding.loginPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.hidePassword.setImageResource(R.drawable.ic_hide_eyes)
                binding.loginPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }
    }
}