package com.lbdev.budgetbuzz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lbdev.budgetbuzz.data.model.Expense
import com.lbdev.budgetbuzz.data.model.Income

class TransactionsRepository {
    private val fireStoreDB = FirebaseFirestore.getInstance()

    fun saveExpense(expense: Expense, callback: (Boolean, Exception?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val documentReference = fireStoreDB.collection("Users").document(uid).collection("Expenses").document()
            documentReference.set(expense)
                .addOnSuccessListener {
                    callback(true, null)
                }.addOnFailureListener { exception ->
                    callback(false, exception)
                }
        } ?: callback(false, Exception("User not logged in"))
    }

    fun saveIncome(income: Income, callback: (Boolean, Exception?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val documentReference = fireStoreDB.collection("Users").document(uid).collection("Incomes").document()
            documentReference.set(income)
                .addOnSuccessListener {
                    callback(true, null)
                }.addOnFailureListener { exception ->
                    callback(false, exception)
                }
        } ?: callback(false, Exception("User not logged in"))
    }
}