package com.lbdev.budgetbuzz.ui.view

import android.os.Bundle
import androidx.activity.viewModels
import com.lbdev.budgetbuzz.R
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

        homeBinding.bottomNavigationView.background = null
        homeBinding.bottomNavigationView.menu.getItem(2).isEnabled = false

        homeBinding.addFab.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            transaction.replace(android.R.id.content, AddTransactionFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        profileViewModel.getUserProfile(auth.uid!!).observe(this) { profile ->
            if (profile != null) {

            }
        }
    }
}