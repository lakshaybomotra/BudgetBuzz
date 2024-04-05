package com.lbdev.budgetbuzz.data.model

import com.google.firebase.Timestamp

data class Income(
    val category: String, val amount: String, val date: Timestamp, val note: String
)
