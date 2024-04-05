package com.lbdev.budgetbuzz.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lbdev.budgetbuzz.data.model.Expense
import com.lbdev.budgetbuzz.data.model.Income
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository

class TransactionViewModel(private val transactionsRepository: TransactionsRepository) :
    ViewModel() {

    private val _savedTransaction = MutableLiveData<String>()
    val savedTransaction: MutableLiveData<String> = _savedTransaction

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    fun saveUserExpenseToDatabase(expense: Expense) {
        transactionsRepository.saveExpense(expense) { success, exception ->
            if (success) {
                _savedTransaction.postValue("Added")
            } else {
                _error.postValue(exception?.localizedMessage ?: "An unknown error occurred")
            }
        }
    }

    fun saveUserIncomeToDatabase(income: Income) {
        transactionsRepository.saveIncome(income) { success, exception ->
            if (success) {
                _savedTransaction.postValue("Added")
            } else {
                _error.postValue(exception?.localizedMessage ?: "An unknown error occurred")
            }
        }
    }
}