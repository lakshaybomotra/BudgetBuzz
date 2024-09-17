package com.lbdev.budgetbuzz.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.lbdev.budgetbuzz.ui.adaptor.OnBoardingAdaptor
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.IntroSlide
import com.lbdev.budgetbuzz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mainActivityVB : ActivityMainBinding
    private lateinit var pageIndicator: LinearLayout

    private val onBoardingAdaptor = OnBoardingAdaptor(
        listOf(
            IntroSlide(
                "Welcome to Budget Planner",
                "Track your expenses and take control of your money effortlessly.",
                R.drawable.illustrationonboard1
            ),
            IntroSlide(
                "Set Smart Goals",
                "Plan your savings journey and achieve your financial objectives with ease.",
                R.drawable.illustrationonboard2
            ),
            IntroSlide(
                "Visualize Your Finances",
                "Gain insights into your spending habits and make informed decisions.",
                R.drawable.illustrationonboard3
            )
        )
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        mainActivityVB = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(mainActivityVB.root)
        checkOnBoarding()
        val introSliderViewPager = mainActivityVB.onBdgViewPager
        introSliderViewPager.adapter = onBoardingAdaptor

        pageIndicator = mainActivityVB.pageIndicator
        setupIndicators()
        setCurrentIndicator(0)

        introSliderViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
        
        mainActivityVB.getStartedBtn.setOnClickListener {
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("onBoardingStatus", false).apply()
            startActivity(Intent(this, LoginSignupActivity::class.java))
            finish()
        }
    }

    private fun checkOnBoarding() {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this).apply {
            val onBoardingStatus = getBoolean("onBoardingStatus", true)
            if (!onBoardingStatus) {
                startActivity(Intent(this@MainActivity, LoginSignupActivity::class.java))
                finish()
            }
        }
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(onBoardingAdaptor.itemCount)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(40, 40)
        layoutParams.setMargins(24, 8, 12,8)

        for (i in indicators.indices)
        {
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.rotation = 45F
                this?.layoutParams = layoutParams
            }
            pageIndicator.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = pageIndicator.childCount
        for (i in 0 until childCount) {
            val imageView = pageIndicator.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }
}