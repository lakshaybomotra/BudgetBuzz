package com.lbdev.budgetbuzz.ui.view

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.repository.CategoriesRepository
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository
import com.lbdev.budgetbuzz.databinding.ActivityHomeBinding
import com.lbdev.budgetbuzz.ui.base.BaseActivity
import com.lbdev.budgetbuzz.ui.viewmodel.CategoryViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.SharedViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.TransactionsViewModel

class HomeActivity : BaseActivity() {
    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var transactionsRepository: TransactionsRepository
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoriesRepository: CategoriesRepository
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var homeBinding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        homeBinding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(homeBinding.root)

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
            with (sharedPref.edit()) {
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
}