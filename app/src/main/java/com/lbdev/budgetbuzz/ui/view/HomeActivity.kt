package com.lbdev.budgetbuzz.ui.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.room.Room
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.db.AppDatabase
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import com.lbdev.budgetbuzz.databinding.ActivityHomeBinding
import com.lbdev.budgetbuzz.ui.base.BaseActivity
import com.lbdev.budgetbuzz.ui.viewmodel.ProfileViewModel
import com.lbdev.budgetbuzz.util.ProfileViewModelFactory

class HomeActivity : BaseActivity() {

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(db.userProfileDao()))
    }
    private lateinit var homeBinding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        homeBinding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(homeBinding.root)

        profileViewModel.getUserProfile(auth.uid!!).observe(this) { profile ->
            if (profile != null) {
                homeBinding.textView.text = profile.name
            }
        }
    }
}