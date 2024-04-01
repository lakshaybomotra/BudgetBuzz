package com.lbdev.budgetbuzz.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lbdev.budgetbuzz.data.db.AppDatabase

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var db: AppDatabase
    protected lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "budget_buzz_db"
        ).build()
    }
}