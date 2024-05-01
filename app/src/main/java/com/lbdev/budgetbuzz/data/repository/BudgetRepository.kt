package com.lbdev.budgetbuzz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lbdev.budgetbuzz.data.model.Budget

class BudgetRepository {
    private val fireStoreDB = FirebaseFirestore.getInstance()

    fun saveBudget(budget: Budget, callback: (Boolean, Exception?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val documentReference =
                fireStoreDB.collection("Users").document(uid).collection("Budget").document("Budget")
            documentReference.set(budget)
                .addOnSuccessListener {
                    callback(true, null)
                }.addOnFailureListener { exception ->
                    callback(false, exception)
                }
        } ?: callback(false, Exception("User not logged in"))
    }

    fun getBudget(callback: (Budget?, Exception?) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val documentReference =
                fireStoreDB.collection("Users").document(uid).collection("Budget").document("Budget")
            documentReference.get()
                .addOnSuccessListener { document ->
                    val budget = document.toObject(Budget::class.java)
                    callback(budget, null)
                }.addOnFailureListener { exception ->
                    callback(null, exception)
                }
        } ?: callback(null, Exception("User not logged in"))
    }
}