package com.lbdev.budgetbuzz.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lbdev.budgetbuzz.data.model.Budget
import com.lbdev.budgetbuzz.data.repository.BudgetRepository

class BudgetViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {
    private val _savedBudget = MutableLiveData<String>()
    val savedBudget: MutableLiveData<String> = _savedBudget

    private val _budget = MutableLiveData<Budget?>()
    val budget: MutableLiveData<Budget?> = _budget

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun saveUserBudget(budget: Budget) {
        budgetRepository.saveBudget(budget) { success, exception ->
            if (success) {
                _savedBudget.postValue("Added")
            } else {
                _error.postValue(exception?.localizedMessage ?: "An unknown error occurred")
            }
        }
    }

    fun getUserBudget() {
        budgetRepository.getBudget { budget, exception ->
            if (budget != null) {
                _budget.postValue(budget)
            } else {
                _error.postValue(exception?.localizedMessage ?: "An unknown error occurred")
            }
        }
    }
}