package com.lbdev.budgetbuzz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lbdev.budgetbuzz.data.model.Transaction

class TransactionsRepository {
    private val fireStoreDB = FirebaseFirestore.getInstance()

    fun saveTransaction(transaction: Transaction, callback: (Boolean, Exception?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val documentReference =
                fireStoreDB.collection("Users").document(uid).collection("Transactions").document()
            documentReference.set(transaction)
                .addOnSuccessListener {
                    callback(true, null)
                }.addOnFailureListener { exception ->
                    callback(false, exception)
                }
        } ?: callback(false, Exception("User not logged in"))
    }

    fun getTransactions(callback: (List<Transaction>, Exception?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            fireStoreDB.collection("Users").document(uid).collection("Transactions").get()
                .addOnSuccessListener { result ->
                    val transaction = result.toObjects(Transaction::class.java)
                    callback(transaction, null)
                }.addOnFailureListener { exception ->
                    callback(emptyList(), exception)
                }
        } ?: callback(emptyList(), Exception("User not logged in"))
    }
}