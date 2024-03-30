package com.lbdev.budgetbuzz.ui.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.IntroSlide

class OnBoardingAdaptor(private val introSlides: List<IntroSlide>)
    : RecyclerView.Adapter<OnBoardingAdaptor.IntroSlideViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroSlideViewHolder {
        return IntroSlideViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.onboarding_slider,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: IntroSlideViewHolder, position: Int) {
        holder.bind(introSlides[position])
    }

    override fun getItemCount(): Int {
        return introSlides.size
    }

    inner class IntroSlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val textHeading = view.findViewById<TextView>(R.id.heading)
        private val textDesc = view.findViewById<TextView>(R.id.description)
        private val image = view.findViewById<ImageView>(R.id.onBdgImg)

        fun bind(introSlide: IntroSlide) {
            textHeading.text = introSlide.title
            textDesc.text = introSlide.description
            image.setImageResource(introSlide.image)
        }
    }
}
