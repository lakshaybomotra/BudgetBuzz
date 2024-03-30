package com.lbdev.budgetbuzz.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import com.lbdev.budgetbuzz.databinding.ActivityVerifyPinBinding
import com.lbdev.budgetbuzz.ui.viewmodel.ProfileViewModel
import com.lbdev.budgetbuzz.util.ProfileViewModelFactory

class VerifyPinActivity : AppCompatActivity() {
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository())
    }
    private lateinit var vpBinding: ActivityVerifyPinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        vpBinding = ActivityVerifyPinBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(vpBinding.root)

        profileViewModel.getSavedProfile()

        vpBinding.verifyUserPinBtn.setOnClickListener {
            val pin = vpBinding.userPin.text.toString()
            if (pin.isEmpty()) {
                vpBinding.userPin.error = "Please enter a pin"
                return@setOnClickListener
            }

            profileViewModel.savedProfile.observe(this) { profile ->
                if (profile != null) {
                    if (profile.pin == pin) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        vpBinding.userPin.error = "Incorrect pin"
                    }
                }
            }
        }
    }
}