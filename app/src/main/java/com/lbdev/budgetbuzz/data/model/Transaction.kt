package com.lbdev.budgetbuzz.data.model

import com.google.firebase.Timestamp
data class Transaction(
    val category: Category, val amount: String, val date: Timestamp, val note: String, val type: String
) {
    constructor() : this(Category(), "", Timestamp.now(), "","")
}