package com.lbdev.budgetbuzz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lbdev.budgetbuzz.databinding.ActivityLoginSignupBinding

class LoginSignupActivity : AppCompatActivity() {
    private lateinit var lsBinding: ActivityLoginSignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        lsBinding = ActivityLoginSignupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(lsBinding.root)


    }
}