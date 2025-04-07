package com.project.agritech.onboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.agritech.R
import com.project.agritech.login.LoginActivity
import com.project.agritech.signup.SignupActivity

class LoginBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet, container, false)

        val btnLogin: Button = view.findViewById(R.id.btnLogin)
        val btnSignUp: Button = view.findViewById(R.id.btnSignUp)

        btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            dismiss() // Close BottomSheetDialogFragment when Login is clicked
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(requireContext(), SignupActivity::class.java))
            dismiss() // Close BottomSheetDialogFragment when Sign Up is clicked
        }

        return view
    }
}