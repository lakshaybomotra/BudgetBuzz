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


//    fun loadCategories() {
//        expenseCategories.value = loadExpenseCategories()
//        incomeCategories.value = loadIncomeCategories()
//    }

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
    fun saveCategory(category: Category, callback: (Boolean, Exception?) -> Unit) {
        categoriesRepository.saveCategory(category) { success, exception ->
            callback(success, exception)
        }
    }
}