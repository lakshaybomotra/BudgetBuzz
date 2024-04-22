package com.lbdev.budgetbuzz.ui.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lbdev.budgetbuzz.data.model.Profile
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import com.lbdev.budgetbuzz.databinding.ActivityCreateProfileBinding
import com.lbdev.budgetbuzz.ui.base.BaseActivity
import com.lbdev.budgetbuzz.ui.viewmodel.ProfileViewModel
import com.lbdev.budgetbuzz.util.ProfileViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class CreateProfileActivity : BaseActivity() {
    lateinit var cpBinding: ActivityCreateProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(db.userProfileDao()))
    }

    private lateinit var profilePic: Bitmap
    private var pin: String = ""
    private var name: String = ""
    private var email: String = ""

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        cpBinding = ActivityCreateProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(cpBinding.root)
        auth = Firebase.auth
        cpBinding.passcode.requestFocus()

        val executor = ContextCompat.getMainExecutor(this)

//        showBiometricPrompt(executor)

        cpBinding.continuePinBtn.setOnClickListener {
            if (cpBinding.passcode.text.toString().length == 4) {
                cpBinding.getPinView.visibility = View.INVISIBLE
                val mSlideRight = Slide()
                mSlideRight.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(cpBinding.createPinView, mSlideRight)
                cpBinding.confirmPinView.visibility = View.VISIBLE
                cpBinding.verifyPasscode.requestFocus()
            } else {
                cpBinding.passcode.error = "Enter 4 digit pin"
            }
        }

        cpBinding.savePinBtn.setOnClickListener {
            if (cpBinding.verifyPasscode.text.toString().length == 4) {
                if (cpBinding.passcode.text.toString() == cpBinding.verifyPasscode.text.toString()) {
                    Toast.makeText(this, "Pin created successfully", Toast.LENGTH_SHORT).show()
                    androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString("uPin", cpBinding.verifyPasscode.text.toString()).apply()

                    pin = cpBinding.verifyPasscode.text.toString()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(cpBinding.root.windowToken, 0)

                    cpBinding.confirmPinView.visibility = View.INVISIBLE
                    val mSlideRight = Slide()
                    mSlideRight.slideEdge = Gravity.END
                    TransitionManager.beginDelayedTransition(cpBinding.createPinView, mSlideRight)
                    cpBinding.profilePicView.visibility = View.VISIBLE
                } else {
                    cpBinding.verifyPasscode.error = "Pin does not match"
                }
            } else {
                cpBinding.verifyPasscode.error = "Enter 4 digit pin"
            }
        }

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                cpBinding.profilePic.setImageURI(uri)
                cpBinding.lookGoodTV.visibility = View.VISIBLE
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        cpBinding.changePicBtn.setOnClickListener {
//            selectPhotoFromGallery.launch("image/*")
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        cpBinding.saveImageBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                profilePic = cpBinding.profilePic.drawable.toBitmap()
                withContext(Dispatchers.Main) {
                    cpBinding.profilePicView.visibility = View.INVISIBLE
                    val mSlideRight = Slide()
                    mSlideRight.slideEdge = Gravity.END
                    TransitionManager.beginDelayedTransition(cpBinding.createPinView, mSlideRight)
                    cpBinding.personalDetailsView.visibility = View.VISIBLE
                }
            }
        }

        cpBinding.savePersonalDetailsBtn.setOnClickListener {
            if (cpBinding.personName.text.toString().isNotEmpty()) {
                name = cpBinding.personName.text.toString()
                cpBinding.personName.error = null
            } else {
                cpBinding.personName.error = "Enter name"
                return@setOnClickListener
            }

            if (cpBinding.personEmail.text.toString().isNotEmpty()) {
                email = cpBinding.personEmail.text.toString()
                cpBinding.personEmail.error = null
            } else {
                cpBinding.personEmail.error = "Enter email"
                return@setOnClickListener
            }
            cpBinding.savePersonalDetailsBtn.isEnabled = false
            cpBinding.saveProfileProgress.visibility = View.VISIBLE

            profileViewModel.saveProfilePicToStorage(profilePic)
        }

        profileViewModel.imageUrl.observe(this) { imageUrl ->
            if (imageUrl != null) {
                val profile = Profile(
                    auth.uid!!,
                    email,
                    name,
                    pin,
                    auth.currentUser?.phoneNumber!!,
                    imageUrl
                )
                profileViewModel.saveProfileToDatabase(profile)
            }
        }

        profileViewModel.savedProfile.observe(this) { profile ->
            if (profile != null) {
                cpBinding.saveProfileProgress.visibility = View.GONE
                Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, VerifyPinActivity::class.java))
                finish()
            }
        }

        profileViewModel.error.observe(this) { error ->
            if (error != null) {
                cpBinding.saveProfileProgress.visibility = View.GONE
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkBiometricSupport(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    val selectPhotoFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val photoUri = it
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(contentResolver, photoUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                }
//                val bitmap =
//                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, photoUri))
                cpBinding.profilePic.setImageBitmap(bitmap)
                cpBinding.lookGoodTV.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBiometricPrompt(executor: Executor) {
        if (checkBiometricSupport()) {
            authUser(executor)
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

    override fun onBackPressed() {
        if (cpBinding.profilePicView.visibility == View.VISIBLE) {
            super.onBackPressed()
        } else if (cpBinding.personalDetailsView.visibility == View.VISIBLE) {
            cpBinding.personalDetailsView.visibility = View.INVISIBLE
            val mSlideRight = Slide()
            mSlideRight.slideEdge = Gravity.START
            TransitionManager.beginDelayedTransition(cpBinding.createPinView, mSlideRight)
            cpBinding.profilePicView.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        val uPin = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
            .getString("uPin", "")
        if (uPin != "") {
            startActivity(Intent(this, VerifyPinActivity::class.java))
            finish()
        }
    }
}