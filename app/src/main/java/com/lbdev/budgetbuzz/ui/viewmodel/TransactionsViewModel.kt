package com.lbdev.budgetbuzz.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lbdev.budgetbuzz.data.model.Transaction
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository

class TransactionsViewModel(private val transactionsRepository: TransactionsRepository) :
    ViewModel() {

    private val _savedTransaction = MutableLiveData<String>()
    val savedTransaction: MutableLiveData<String> = _savedTransaction

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: MutableLiveData<List<Transaction>> = _transactions

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getUserTransaction() {
        transactionsRepository.getTransactions { transactions, exception ->
            _transactions.value = transactions
        }
    }

    fun saveUserTransaction(transaction: Transaction) {
        transactionsRepository.saveTransaction(transaction) { success, exception ->
            if (success) {
                _savedTransaction.postValue("Added")
            } else {
                _error.postValue(exception?.localizedMessage ?: "An unknown error occurred")
            }
        }
    }
}