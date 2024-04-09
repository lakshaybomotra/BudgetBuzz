package com.lbdev.budgetbuzz.ui.view

import android.os.Bundle
import androidx.activity.viewModels
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

        this.supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_in_left)
            .replace(R.id.fcvMain, ActivityFragment::class.java, null)
            .commit()

        homeBinding.bottomNavigationView.background = null
        homeBinding.bottomNavigationView.menu.getItem(2).isEnabled = false

        homeBinding.addFab.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            transaction.replace(android.R.id.content, AddTransactionFragment())
            transaction.commit()
        }

        homeBinding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.activity -> {
                    this.supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            android.R.anim.slide_in_left,
                            android.R.anim.slide_in_left
                        )
                        .replace(R.id.fcvMain, ActivityFragment::class.java, null)
                        .commit()
                }

                R.id.overview -> {
                    this.supportFragmentManager.beginTransaction()
                        .replace(R.id.fcvMain, OverviewFragment::class.java, null)
                        .commit()
                }

                R.id.budget -> {
                    this.supportFragmentManager.beginTransaction()
                        .replace(R.id.fcvMain, BudgetFragment::class.java, null)
                        .commit()
                }

                R.id.profile -> {
                    this.supportFragmentManager.beginTransaction()
                        .replace(R.id.fcvMain, ProfileFragment::class.java, null)
                        .commit()
                }
            }
            true
        }
    }
}