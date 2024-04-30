package com.lbdev.budgetbuzz.ui.view

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.repository.CategoriesRepository
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository
import com.lbdev.budgetbuzz.databinding.ActivityHomeBinding
import com.lbdev.budgetbuzz.ui.base.BaseActivity
import com.lbdev.budgetbuzz.ui.viewmodel.CategoryViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.SharedViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.TransactionsViewModel

class HomeActivity : BaseActivity() {
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
            val editor = notificationPref.edit()
            editor.putBoolean("notificationEnabled", true)
            editor.apply()
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            val editor = notificationPref.edit()
            editor.putBoolean("notificationEnabled", false)
            editor.apply()
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var transactionsRepository: TransactionsRepository
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoriesRepository: CategoriesRepository
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var homeBinding: ActivityHomeBinding
    private var isNotificationEnabled = true
    lateinit var notificationPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        homeBinding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(homeBinding.root)
        notificationPref = getSharedPreferences("notificationPref", Context.MODE_PRIVATE)
        askNotificationPermission()
        categoriesRepository = CategoriesRepository()
        categoryViewModel = CategoryViewModel(categoriesRepository)
        categoryViewModel.loadCategories()
        transactionsRepository = TransactionsRepository()
        transactionsViewModel = TransactionsViewModel(transactionsRepository)
        transactionsViewModel.getUserTransaction()
        categoryViewModel.expenseCategories.observe(this) { list ->
            sharedViewModel.addExpenseCategory(list)
        }
        categoryViewModel.incomeCategories.observe(this) { list ->
            sharedViewModel.addIncomeCategory(list)
        }

        homeBinding.bottomNavigationView.background = null
        homeBinding.bottomNavigationView.menu.getItem(2).isEnabled = false

        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val lastSelectedItemId = sharedPref.getInt("LastSelectedItemId", R.id.activity)

        homeBinding.addFab.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            transaction.replace(android.R.id.content, AddTransactionFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        homeBinding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            with(sharedPref.edit()) {
                putInt("LastSelectedItemId", item.itemId)
                apply()
            }
            val currentFragment = supportFragmentManager.findFragmentByTag(item.itemId.toString())
            if (currentFragment == null) {
                when (item.itemId) {
                    R.id.activity -> replaceFragment(ActivityFragment(), item.itemId.toString())
                    R.id.overview -> replaceFragment(OverviewFragment(), item.itemId.toString())
                    R.id.budget -> replaceFragment(BudgetFragment(), item.itemId.toString())
                    R.id.profile -> replaceFragment(ProfileFragment(), item.itemId.toString())
                }
            }
            true
        }
        homeBinding.bottomNavigationView.selectedItemId = lastSelectedItemId
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            replace(R.id.fcvMain, fragment, tag)
            commit()
        }
    }

    override fun onBackPressed() {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("LastSelectedItemId")
            apply()
        }
        super.onBackPressed()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
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

                isNotificationEnabled = notificationPref.getBoolean("notificationEnabled", false)
                if (isNotificationEnabled) {
                    FirebaseMessaging.getInstance().subscribeToTopic("budgetbuzz")
                        .addOnCompleteListener { task ->
                            var msg = "Subscribed to topic: budgetbuzz"
                            if (!task.isSuccessful) {
                                msg = "Failed to subscribe to topic: budgetbuzz"
                            }
                            Log.d("firebaseMessaging", msg)
                        }
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("budgetbuzz")
                        .addOnCompleteListener { task ->
                            var msg = "Unsubscribed from topic: budgetbuzz"
                            if (!task.isSuccessful) {
                                msg = "Failed to unsubscribe from topic: budgetbuzz"
                            }
                            Log.d("firebaseMessaging", msg)
                        }
                }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}