package com.project.agritech.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.project.agritech.R
import com.project.agritech.api.ApiUtilities
import com.project.agritech.databinding.ActivityViewProfileBinding
import com.project.agritech.editprofile.EditProfileActivity
import com.project.agritech.login.LoginActivity
import com.project.agritech.resetpassword.ResetPasswordActivity
import com.project.agritech.utlis.SharedPreferencesManager
import com.project.agritech.waterusage.AnalysisActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewProfileBinding
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var phone: TextView
    private lateinit var address: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        sharedPreferencesManager = SharedPreferencesManager(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.editProfile.setOnClickListener {
            val intent = Intent(applicationContext, EditProfileActivity::class.java)
            startActivity(intent)

        }

        binding.btnWaterUsageContainer.setOnClickListener {
            val intent = Intent(applicationContext, AnalysisActivity::class.java)
            startActivity(intent)
        }

        binding.backArrow.setOnClickListener {
           // finish()
            onBackPressed()
        }

        binding.btnResetPassContainer.setOnClickListener {
            val intent = Intent(applicationContext, ResetPasswordActivity::class.java)
            startActivity(intent)
//            finish()
        }


        name = binding.nameTv
        email = binding.emailTv
        phone = binding.phoneTv
        address = binding.addressTv

        val userId: String = sharedPreferencesManager.getUserId()
        viewProfile(userId)

        binding.btnLogoutContainer.setOnClickListener {
            // Inflate the custom layout for the dialog
            val dialogView = layoutInflater.inflate(R.layout.logout_dialog, null)

            // Create AlertDialog with custom style
            val alertDialog = AlertDialog.Builder(this, R.style.TransparentDialog)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            // Set background transparency and position it properly
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            alertDialog.window?.setGravity(Gravity.CENTER)

            alertDialog.show()

            // Handling button clicks inside the custom dialog
            val btnYes = dialogView.findViewById<AppCompatButton>(R.id.btnYes)
            val btnNo = dialogView.findViewById<AppCompatButton>(R.id.btnNo)

            btnYes.setOnClickListener {
                sharedPreferencesManager.saveLoginStatus("loggedOut")

                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
               // finish()

                alertDialog.dismiss()
            }

            btnNo.setOnClickListener {
                alertDialog.dismiss()
            }

        }
    }

    private fun viewProfile(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = ApiUtilities.getInstance()
                val response = apiService.getUserProfile(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val profileData = response.body()!!.userData

                        binding.nameTv.text = profileData[0].name
                        binding.emailTv.text = profileData[0].email
                        binding.phoneTv.text = profileData[0].phone
                        binding.addressTv.text = profileData[0].address

// Load the profile image using Glide
                        val profileImageView: ImageView = binding.profileImage
                        if (!profileData[0].photo.isNullOrEmpty()) {
                            Glide.with(this@ViewProfileActivity)
                                .load(profileData[0].photo)
                                .placeholder(R.drawable.profile_icon) // Placeholder image
                                .error(R.drawable.error_image) // Error image
                                .into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.profile_icon)
                        }

                    } else {
                        Toast.makeText(
                            this@ViewProfileActivity,
                            "Server error: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ViewProfileActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}