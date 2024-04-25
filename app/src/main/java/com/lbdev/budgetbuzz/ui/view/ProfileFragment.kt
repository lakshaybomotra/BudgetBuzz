package com.lbdev.budgetbuzz.ui.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.room.Room
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.db.AppDatabase
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import com.lbdev.budgetbuzz.databinding.FragmentProfileBinding
import com.lbdev.budgetbuzz.ui.viewmodel.ProfileViewModel
import com.lbdev.budgetbuzz.util.ProfileViewModelFactory

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db by lazy {
        Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "budget_buzz_db"
        ).build()
    }
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(db.userProfileDao()))
    }
    private var biometricPref: SharedPreferences? = null
    private var isBiometricEnabled = false
    private var notificationPref: SharedPreferences? = null
    private var isNotificationEnabled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        profileViewModel.getFirebaseProfile()
        profileViewModel.savedProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                profileViewModel.saveUserProfile(profile)
            }
        }

        profileViewModel.getUserProfile(auth.currentUser!!.uid)
            .observe(viewLifecycleOwner) { profile ->
                binding.profileName.text = profile.name
                binding.profileNumber.text = profile.phone
                binding.profilePic.load(profile.image) {
                    placeholder(R.drawable.account_profile_dummy)
                }
            }

        val fingerPrintSwitch = binding.fingerprintSwitch
        biometricPref =
            requireActivity().getSharedPreferences("biometricPref", Context.MODE_PRIVATE)
        isBiometricEnabled = biometricPref!!.getBoolean("biometricEnabled", false)

        if (isBiometricEnabled) {
            fingerPrintSwitch.trackDecorationTintList =
                resources.getColorStateList(R.color.green_income, null)
            fingerPrintSwitch.trackTintList =
                resources.getColorStateList(R.color.green_income, null)
        } else {
            fingerPrintSwitch.trackDecorationTintList =
                resources.getColorStateList(R.color.red_expense, null)
            fingerPrintSwitch.trackTintList = resources.getColorStateList(R.color.red_expense, null)
        }
        fingerPrintSwitch.isChecked = isBiometricEnabled

        fingerPrintSwitch.setOnCheckedChangeListener { _, b ->
            isBiometricEnabled = b
            val editor = biometricPref!!.edit()
            editor.putBoolean("biometricEnabled", isBiometricEnabled)
            editor.apply()
            if (b) {
                fingerPrintSwitch.trackDecorationTintList =
                    resources.getColorStateList(R.color.green_income, null)
                fingerPrintSwitch.trackTintList =
                    resources.getColorStateList(R.color.green_income, null)
            } else {
                fingerPrintSwitch.trackDecorationTintList =
                    resources.getColorStateList(R.color.red_expense, null)
                fingerPrintSwitch.trackTintList =
                    resources.getColorStateList(R.color.red_expense, null)
            }
        }
//        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//            if (uri != null) {
//                val imageView = view.findViewById<ImageView>(R.id.pImage)
//                imageView.setImageURI(uri)
//                Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT).show()
//                Log.d("PhotoPicker", "Selected URI: $uri")
//            } else {
//                Log.d("PhotoPicker", "No media selected")
//            }
//        }
        binding.changePasscodeButton.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePinActivity::class.java))
        }

        val notificationSwitch = binding.notificationSwitch
        notificationPref =
            requireActivity().getSharedPreferences("notificationPref", Context.MODE_PRIVATE)
        isNotificationEnabled = notificationPref!!.getBoolean("notificationEnabled", false)

        if (isNotificationEnabled) {
            notificationSwitch.trackDecorationTintList =
                resources.getColorStateList(R.color.green_income, null)
            notificationSwitch.trackTintList =
                resources.getColorStateList(R.color.green_income, null)
        } else {
            notificationSwitch.trackDecorationTintList =
                resources.getColorStateList(R.color.red_expense, null)
            notificationSwitch.trackTintList =
                resources.getColorStateList(R.color.red_expense, null)
        }
        notificationSwitch.isChecked = isNotificationEnabled

        notificationSwitch.setOnCheckedChangeListener { _, b ->
            isNotificationEnabled = b
            val editor = notificationPref!!.edit()
            editor.putBoolean("notificationEnabled", isNotificationEnabled)
            editor.apply()
            if (b) {
                notificationSwitch.trackDecorationTintList =
                    resources.getColorStateList(R.color.green_income, null)
                notificationSwitch.trackTintList =
                    resources.getColorStateList(R.color.green_income, null)
            } else {
                notificationSwitch.trackDecorationTintList =
                    resources.getColorStateList(R.color.red_expense, null)
                notificationSwitch.trackTintList =
                    resources.getColorStateList(R.color.red_expense, null)
            }
        }

        binding.supportButton.setOnClickListener {
            val mIntent = Intent(Intent.ACTION_SEND)
            mIntent.setType("message/rfc822")
            mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("budgetbuzz@gmail.com"))
            mIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request")
            mIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hello, I am facing some issues with the app. Please help me out."
            )

            try {
                startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }

        binding.shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this app!")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "https://landing.flycricket.io/bazinga/9de78df1-c2a3-4b60-a033-cd6cab92cc6b/"
                )
            }

            startActivity(Intent.createChooser(shareIntent, "Share app using:"))
        }

        binding.termsButton.setOnClickListener {
            binding.webview.visibility = View.VISIBLE
            val privacyPolicyUrl =
                "https://doc-hosting.flycricket.io/bazinga-privacy-policy/977c683e-5457-4457-8539-3c311e501e34/privacy"
            binding.webview.loadUrl(privacyPolicyUrl)
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginSignupActivity::class.java))
            requireActivity().finish()
        }

        return view
    }
}