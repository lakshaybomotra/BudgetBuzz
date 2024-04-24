package com.lbdev.budgetbuzz.ui.base

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lbdev.budgetbuzz.data.db.AppDatabase
import com.lbdev.budgetbuzz.data.repository.ProfileRepository
import com.lbdev.budgetbuzz.ui.viewmodel.ProfileViewModel
import com.lbdev.budgetbuzz.util.ProfileViewModelFactory

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var db: AppDatabase
    protected lateinit var auth: FirebaseAuth
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(db.userProfileDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "budget_buzz_db"
        ).build()

        profileViewModel.getFirebaseProfile()
        profileViewModel.savedProfile.observe(this) { profile ->
            if (profile != null) {
                profileViewModel.saveUserProfile(profile)
            }
        }
    }
}