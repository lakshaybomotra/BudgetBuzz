package com.lbdev.budgetbuzz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey val userID: String,
    val email: String,
    val name: String,
    val pin: String,
    val phone: String,
    val image: String
) {
    constructor() : this("", "", "", "", "", "")
}
