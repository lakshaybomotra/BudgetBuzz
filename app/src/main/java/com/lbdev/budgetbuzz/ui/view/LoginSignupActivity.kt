package com.lbdev.budgetbuzz.ui.view

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.databinding.ActivityLoginSignupBinding
import com.lbdev.budgetbuzz.ui.base.BaseActivity
import java.util.concurrent.TimeUnit

class LoginSignupActivity : BaseActivity() {
    private lateinit var lsBinding: ActivityLoginSignupBinding
//    private var storedVerificationId: String? = ""
//    private var phoneNumber: String? = ""
//    private lateinit var resendToken: ForceResendingToken
//    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        lsBinding = ActivityLoginSignupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(lsBinding.root)
        auth = Firebase.auth
//        lsBinding.ccp.registerCarrierNumberEditText(lsBinding.editTextCarrierNumber)

        lsBinding.continueBtn.setOnClickListener {
            if (lsBinding.etEmail.text.toString().isEmpty() && lsBinding.etPassword.text.toString().isEmpty())
            {
                Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lsBinding.progressBar.visibility = View.VISIBLE
            lsBinding.continueBtn.isEnabled = false
            welcomeUser()
        }

//        lsBinding.verifyOtpBtn.setOnClickListener {
//            val otp = lsBinding.otpLL.text.toString()
//            if (otp.isEmpty() || otp.length < 6) {
//                lsBinding.otpLL.error = "Enter OTP"
//                return@setOnClickListener
//            } else {
//                lsBinding.otpLL.error = null
//                lsBinding.progressBar.visibility = View.VISIBLE
//                verifyPhoneNumberWithCode(storedVerificationId, otp)
//            }
//        }

//        lsBinding.resendOtpTv.setOnClickListener {
//            resendVerificationCode(phoneNumber.toString(), resendToken)
//        }

//        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                Log.d(TAG, "onVerificationCompleted:$credential")
//            }
//
//            override fun onVerificationFailed(e: FirebaseException) {
//                when (e) {
//                    is FirebaseAuthInvalidCredentialsException -> {
//                        Toast.makeText(
//                            this@LoginSignupActivity,
//                            "Invalid Phone Number",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                    is FirebaseTooManyRequestsException -> {
//                        Toast.makeText(
//                            this@LoginSignupActivity,
//                            "Too many requests",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                    is FirebaseAuthMissingActivityForRecaptchaException -> {
//                        Toast.makeText(
//                            this@LoginSignupActivity,
//                            "Missing ReCaptcha",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                    else -> {
//                        Toast.makeText(
//                            this@LoginSignupActivity,
//                            "Verification Failed",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            }
//
//            override fun onCodeSent(
//                verificationId: String,
//                token: ForceResendingToken,
//            ) {
//                storedVerificationId = verificationId
//                resendToken = token
//                lsBinding.getStartedTV.text = getString(R.string.verification_code)
//                lsBinding.codeSentTv.text =
//                    getString(
//                        R.string.enter_the_verification_code_sent_to,
//                        lsBinding.ccp.fullNumberWithPlus
//                    )
//
//                lsBinding.constraintNumber.visibility = View.INVISIBLE
//                lsBinding.progressBar.visibility = View.INVISIBLE
//                val mSlideRight = Slide()
//                mSlideRight.slideEdge = Gravity.END
//                TransitionManager.beginDelayedTransition(lsBinding.relativeMain, mSlideRight)
//                lsBinding.constraintOtp.visibility = View.VISIBLE
//                lsBinding.otpLL.requestFocus()
//                lsBinding.continueBtn.isEnabled = true
//                Toast.makeText(this@LoginSignupActivity, "Otp Sent", Toast.LENGTH_SHORT).show()
//                startTimer()
//            }
//        }
    }

    private fun welcomeUser() {
        auth.createUserWithEmailAndPassword(lsBinding.etEmail.text.toString(), lsBinding.etPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    lsBinding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    profileViewModel.getFirebaseProfile()
                    profileViewModel.isLoading.observe(this) { isLoading ->
                        if (!isLoading) {
                            profileViewModel.savedProfile.observe(this) { profile ->
                                if (profile != null) {
                                    startActivity(Intent(this, VerifyPinActivity::class.java))
                                    finish()
                                } else {
                                    startActivity(Intent(this, CreateProfileActivity::class.java))
                                    finish()
                                }
                            }
                        }
                    }
                } else {
                    when (task.exception) {
                        is FirebaseAuthUserCollisionException -> {
                            signInUser()
                        }
                    }
                }
            }
    }

    private fun signInUser() {
        val email = lsBinding.etEmail.text.toString()
        val password = lsBinding.etPassword.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    profileViewModel.getFirebaseProfile()
                    profileViewModel.isLoading.observe(this) { isLoading ->
                        if (!isLoading) {
                            profileViewModel.savedProfile.observe(this) { profile ->
                                if (profile != null) {
                                    startActivity(Intent(this, VerifyPinActivity::class.java))
                                    finishAffinity()
                                } else {
                                    startActivity(Intent(this, CreateProfileActivity::class.java))
                                    finishAffinity()
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

//    private fun startPhoneNumberVerification(phoneNumber: String) {
//        val options = PhoneAuthOptions.newBuilder(auth)
//            .setPhoneNumber(phoneNumber)
//            .setTimeout(60L, TimeUnit.SECONDS)
//            .setActivity(this)
//            .setCallbacks(callbacks)
//            .build()
//        PhoneAuthProvider.verifyPhoneNumber(options)
//    }
//
//    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
//        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
//        signInWithPhoneAuthCredential(credential)
//    }
//
//    private fun resendVerificationCode(
//        phoneNumber: String,
//        token: ForceResendingToken
//    ) {
//        val options = PhoneAuthOptions.newBuilder(auth)
//            .setPhoneNumber(phoneNumber)
//            .setTimeout(60L, TimeUnit.SECONDS)
//            .setActivity(this)
//            .setCallbacks(callbacks)
//            .setForceResendingToken(token)
//            .build()
//        PhoneAuthProvider.verifyPhoneNumber(options)
//    }

//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    lsBinding.progressBar.visibility = View.INVISIBLE
//                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
//                    profileViewModel.getFirebaseProfile()
//                    profileViewModel.isLoading.observe(this) { isLoading ->
//                        if (!isLoading) {
//                            profileViewModel.savedProfile.observe(this) { profile ->
//                                if (profile != null) {
//                                    startActivity(Intent(this, VerifyPinActivity::class.java))
//                                    finish()
//                                } else {
//                                    startActivity(Intent(this, CreateProfileActivity::class.java))
//                                    finish()
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        Toast.makeText(this, "Invalid Otp...", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//    }

//    override fun onBackPressed() {
//        if (lsBinding.constraintOtp.visibility == View.VISIBLE) {
//            lsBinding.constraintOtp.visibility = View.INVISIBLE
//            val mSlideLeft = Slide()
//            mSlideLeft.slideEdge = Gravity.START
//            TransitionManager.beginDelayedTransition(lsBinding.relativeMain, mSlideLeft)
//            lsBinding.constraintNumber.visibility = View.VISIBLE
//        } else {
//            super.onBackPressed()
//        }
//    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, VerifyPinActivity::class.java))
            finish()
        }
    }

//    private fun startTimer() {
//        object : CountDownTimer(60000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                lsBinding.resendOtpTv.isEnabled = false
//                lsBinding.resendOtpTv.text = buildString {
//                    append("Resend in: ")
//                    append(millisUntilFinished / 1000)
//                    append("s")
//                }
//            }
//
//            override fun onFinish() {
//                lsBinding.resendOtpTv.text = getString(R.string.resend)
//                lsBinding.resendOtpTv.isEnabled = true
//            }
//        }.start()
//    }

    companion object {
        const val TAG = "LoginSignupActivity"
    }
}