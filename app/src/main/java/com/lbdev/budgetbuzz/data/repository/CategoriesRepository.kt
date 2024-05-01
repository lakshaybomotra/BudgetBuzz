package com.lbdev.budgetbuzz.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lbdev.budgetbuzz.data.model.Category

class CategoriesRepository {
    private val fireStoreDB = FirebaseFirestore.getInstance()

    fun getExpenseCategories(callback: (List<Category>, Exception?) -> Unit) {
        val documentReference = fireStoreDB.collection("Categories").whereEqualTo("type", "Expense")
        documentReference.get()
            .addOnSuccessListener { result ->
                val categories = result.toObjects(Category::class.java)
                callback(categories, null)
            }
            .addOnFailureListener { exception ->
                callback(emptyList(), exception)
            }
    }

    fun saveCategory(category: Category, callback: (Boolean, Exception?) -> Unit) {
        fireStoreDB.collection("Categories").add(category)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { exception ->
                callback(false, exception)
            }
    }

    fun getIncomeCategories(callback: (List<Category>, Exception?) -> Unit) {
        val documentReference = fireStoreDB.collection("Categories").whereEqualTo("type", "Income")
        documentReference.get()
            .addOnSuccessListener { result ->
                val categories = result.toObjects(Category::class.java)
                callback(categories, null)
            }
            .addOnFailureListener { exception ->
                callback(emptyList(), exception)
            }
    }

    fun getCategories(callback: (List<Category>, Exception?) -> Unit) {
        val documentReference = fireStoreDB.collection("Categories")
        documentReference.get()
            .addOnSuccessListener { result ->
                val categories = result.toObjects(Category::class.java)
                callback(categories, null)
            }
            .addOnFailureListener { exception ->
                callback(emptyList(), exception)
            }
    }
}