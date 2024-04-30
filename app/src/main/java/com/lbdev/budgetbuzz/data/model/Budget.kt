package com.lbdev.budgetbuzz.data.model

class Budget(
    val name: String,
    val amount: String
) {
    constructor() : this("", "")
}