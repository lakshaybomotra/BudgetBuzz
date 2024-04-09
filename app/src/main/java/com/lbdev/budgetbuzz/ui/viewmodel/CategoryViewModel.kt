package com.lbdev.budgetbuzz.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lbdev.budgetbuzz.data.model.Category
import com.lbdev.budgetbuzz.data.repository.CategoriesRepository

class CategoryViewModel(private val categoriesRepository: CategoriesRepository) : ViewModel() {

    private val _expenseCategories = MutableLiveData<List<Category>>()
    val expenseCategories: MutableLiveData<List<Category>> = _expenseCategories

    private val _incomeCategories = MutableLiveData<List<Category>>()
    val incomeCategories: MutableLiveData<List<Category>> = _incomeCategories

    fun loadExpenseCategories() {
        categoriesRepository.getExpenseCategories { categories, exception ->
            _expenseCategories.postValue(categories)
        }
    }

    fun loadIncomeCategories() {
        categoriesRepository.getIncomeCategories { categories, exception ->
            _incomeCategories.postValue(categories)
        }
    }

    fun loadCategories() {
        categoriesRepository.getCategories { categories, exception ->
            _expenseCategories.value = categories.filter { it.type == "Expense" }
            _incomeCategories.value = categories.filter { it.type == "Income" }
        }
    }
}