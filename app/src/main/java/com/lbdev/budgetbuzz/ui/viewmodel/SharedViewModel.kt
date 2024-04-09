package com.lbdev.budgetbuzz.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.Category

class SharedViewModel : ViewModel() {
    val selectedItem: MutableLiveData<Category> = MutableLiveData()
    val expenseCategories: MutableLiveData<List<Category>> = MutableLiveData()
    val incomeCategories: MutableLiveData<List<Category>> = MutableLiveData()
    val isTransactionAdded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun removeSelectedItem() {
        selectedItem.value = Category(
            colorToHex(R.color.activity_background),
            colorToHex(R.color.activity_background),
            "Select a category",
            "",
            ""
        )
    }

    fun addExpenseCategory(categories: List<Category>) {
        expenseCategories.value = categories
    }

    fun addIncomeCategory(categories: List<Category>) {
        incomeCategories.value = categories
    }

    private fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}