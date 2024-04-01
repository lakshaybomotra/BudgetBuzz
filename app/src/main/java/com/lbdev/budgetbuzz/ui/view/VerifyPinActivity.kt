package com.lbdev.budgetbuzz.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.room.Room
import com.lbdev.budgetbuzz.data.db.AppDatabase
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import com.lbdev.budgetbuzz.databinding.ActivityVerifyPinBinding
import com.lbdev.budgetbuzz.ui.base.BaseActivity
import com.lbdev.budgetbuzz.ui.viewmodel.ProfileViewModel
import com.lbdev.budgetbuzz.util.ProfileViewModelFactory

class VerifyPinActivity : BaseActivity() {

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(db.userProfileDao()))
    }
    private lateinit var vpBinding: ActivityVerifyPinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        vpBinding = ActivityVerifyPinBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(vpBinding.root)

        profileViewModel.getSavedProfile()

        vpBinding.verifyUserPinBtn.setOnClickListener {
            val pin = vpBinding.userPin.text.toString()
            if (pin.isEmpty() || pin.length < 4) {
                vpBinding.userPin.error = "Please enter a pin"
                return@setOnClickListener
            }

            profileViewModel.savedProfile.observe(this) { profile ->
                if (profile != null) {
                    profileViewModel.saveUserProfile(profile)
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