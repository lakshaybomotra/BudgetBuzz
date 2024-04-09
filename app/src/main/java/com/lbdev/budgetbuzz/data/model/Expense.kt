package com.lbdev.budgetbuzz.data.model

import com.google.firebase.Timestamp

data class Expense(
    val category: Category, val amount: String, val date: Timestamp, val note: String
) {
    constructor() : this(Category(), "", Timestamp.now(), "")
}
