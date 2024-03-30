package com.lbdev.budgetbuzz.data.model

data class Profile(
    val email: String,
    val name: String,
    val pin: String,
    val phone: String,
    val userID: String,
    val image: String
) {
    constructor() : this("", "", "", "", "", "")
}
