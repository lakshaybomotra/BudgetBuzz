package com.lbdev.budgetbuzz.ui.view

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.lbdev.budgetbuzz.databinding.ActivityChangePinBinding
import com.lbdev.budgetbuzz.ui.base.BaseActivity

class ChangePinActivity : BaseActivity() {
    private lateinit var binding: ActivityChangePinBinding
    private var pin: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.passcode.requestFocus()

        binding.continuePinBtn.setOnClickListener {
            if (binding.passcode.text.toString().length == 4) {
                binding.getPinView.visibility = View.INVISIBLE
                val mSlideRight = Slide()
                mSlideRight.slideEdge = Gravity.END
                TransitionManager.beginDelayedTransition(binding.getPinView, mSlideRight)
                binding.confirmPinView.visibility = View.VISIBLE
                binding.verifyPasscode.requestFocus()
            } else {
                binding.passcode.error = "Enter 4 digit pin"
            }
        }

        binding.savePinBtn.setOnClickListener {
            if (binding.verifyPasscode.text.toString().length == 4) {
                if (binding.passcode.text.toString() == binding.verifyPasscode.text.toString()) {
                    androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString("uPin", binding.verifyPasscode.text.toString()).apply()

                    pin = binding.verifyPasscode.text.toString()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

                    profileViewModel.updatePin(auth.currentUser!!.uid, pin)
                    profileViewModel.pinUpdated.observe(this) {
                        if (it != null) {
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }

                } else {
                    binding.verifyPasscode.error = "Pin does not match"
                }
            } else {
                binding.verifyPasscode.error = "Enter 4 digit pin"
            }
        }
    }
}