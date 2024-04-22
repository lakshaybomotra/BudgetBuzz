package com.lbdev.budgetbuzz.ui.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lbdev.budgetbuzz.R

class ProfileFragment : Fragment() {
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val btn = view.findViewById<View>(R.id.pButton)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val imageView = view.findViewById<ImageView>(R.id.pImage)
                imageView.setImageURI(uri)
                Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT).show()
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        btn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginSignupActivity::class.java))
            requireActivity().finish()
        }
        
        return view
    }
}