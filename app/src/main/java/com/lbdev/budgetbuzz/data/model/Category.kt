package com.lbdev.budgetbuzz.data.model

data class Category(
    val startColor: String,
    val endColor: String,
    val name: String,
    val type: String,
    val icon: String
) {
    constructor() : this("", "", "","", "")
}