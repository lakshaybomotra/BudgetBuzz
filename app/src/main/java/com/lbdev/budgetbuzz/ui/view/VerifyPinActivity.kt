package com.lbdev.budgetbuzz.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import com.lbdev.budgetbuzz.databinding.ActivityVerifyPinBinding
import com.lbdev.budgetbuzz.ui.base.BaseActivity
import com.lbdev.budgetbuzz.ui.viewmodel.ProfileViewModel
import com.lbdev.budgetbuzz.util.ProfileViewModelFactory
import java.util.concurrent.Executor

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

        val executor = ContextCompat.getMainExecutor(this)

        vpBinding.fingerprintIv.setOnClickListener {
            showBiometricPrompt(executor)
        }

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
                        finishAffinity()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        vpBinding.userPin.error = "Incorrect pin"
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(executor: Executor) {
        if (checkBiometricSupport()) {
            authUser(executor)
        }
    }

    private fun checkBiometricSupport(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    private fun authUser(executor: Executor) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Authenticate")
            .setSubtitle("Use your fingerprint to authenticate").setDescription("Touch the sensor")
            .setDeviceCredentialAllowed(true).build()

        val biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    finishAffinity()
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                    Toast.makeText(
                        applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed", Toast.LENGTH_SHORT
                    ).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }
}