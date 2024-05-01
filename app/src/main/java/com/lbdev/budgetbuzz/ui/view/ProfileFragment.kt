package com.lbdev.budgetbuzz.ui.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.room.Room
import coil.load
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.db.AppDatabase
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import com.lbdev.budgetbuzz.databinding.FragmentProfileBinding
import com.lbdev.budgetbuzz.ui.viewmodel.ProfileViewModel
import com.lbdev.budgetbuzz.util.ProfileViewModelFactory

class ProfileFragment : Fragment() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        "firebaseMessaging",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@OnCompleteListener
                }
            })
            val editor = notificationPref!!.edit()
            editor.putBoolean("notificationEnabled", true)
            editor.apply()
        } else {
            showExplanationDialog()
            val editor = notificationPref!!.edit()
            editor.putBoolean("notificationEnabled", false)
            editor.apply()
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    askNotificationPermission()
                } else {
                    FirebaseMessaging.getInstance().subscribeToTopic("budgetbuzz")
                        .addOnCompleteListener { task ->
                            var msg = "Subscribed to topic: budgetbuzz"
                            if (!task.isSuccessful) {
                                msg = "Failed to subscribe to topic: budgetbuzz"
                            }
                            Log.d("firebaseMessaging", msg)
                        }
                }
                askNotificationPermission()
            } else {
                notificationSwitch.trackDecorationTintList =
                    resources.getColorStateList(R.color.red_expense, null)
                notificationSwitch.trackTintList =
                    resources.getColorStateList(R.color.red_expense, null)
                FirebaseMessaging.getInstance().unsubscribeFromTopic("budgetbuzz")
                    .addOnCompleteListener { task ->
                        var msg = "Unsubscribed from topic: budgetbuzz"
                        if (!task.isSuccessful) {
                            msg = "Failed to unsubscribe from topic: budgetbuzz"
                        }
                        Log.d("firebaseMessaging", msg)
                    }
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
            val url = "https://doc-hosting.flycricket.io/bazinga-privacy-policy/977c683e-5457-4457-8539-3c311e501e34/privacy"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginSignupActivity::class.java))
            requireActivity().finish()
        }

        return view
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(
                            "firebaseMessaging",
                            "Fetching FCM registration token failed",
                            task.exception
                        )
                        return@OnCompleteListener
                    }
                })

                isNotificationEnabled = notificationPref!!.getBoolean("notificationEnabled", false)
                if (isNotificationEnabled) {
                    binding.notificationSwitch.isChecked = true
                    FirebaseMessaging.getInstance().subscribeToTopic("budgetbuzz")
                        .addOnCompleteListener { task ->
                            var msg = "Subscribed to topic: budgetbuzz"
                            if (!task.isSuccessful) {
                                msg = "Failed to subscribe to topic: budgetbuzz"
                            }
                            Log.d("firebaseMessaging", msg)
                        }
                } else {
                    binding.notificationSwitch.isChecked = false
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("budgetbuzz")
                        .addOnCompleteListener { task ->
                            var msg = "Unsubscribed from topic: budgetbuzz"
                            if (!task.isSuccessful) {
                                msg = "Failed to unsubscribe from topic: budgetbuzz"
                            }
                            Log.d("firebaseMessaging", msg)
                        }
                }
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showExplanationDialog()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission Needed")
            .setMessage("This app needs the Notification permission to send you notifications. Please grant this permission in App Settings.")
            .setPositiveButton("App Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                binding.notificationSwitch.isChecked = false
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (isNotificationEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    binding.notificationSwitch.isChecked = true
                    val editor = notificationPref!!.edit()
                    editor.putBoolean("notificationEnabled", true)
                    editor.apply()
                } else {
                    binding.notificationSwitch.isChecked = false
                    val editor = notificationPref!!.edit()
                    editor.putBoolean("notificationEnabled", false)
                    editor.apply()
                }
            }
        }
    }
}