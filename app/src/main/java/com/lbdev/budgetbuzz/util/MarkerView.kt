package com.lbdev.budgetbuzz.util

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.lbdev.budgetbuzz.R

class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry, highlight: Highlight) {
        val currencySign = resources.getString(R.string.currencySign)
        tvContent.text = String.format("Date: %s, Amount: ${currencySign}%s", e.x.toInt(), e.y)

        if (e.y > 0) {
            tvContent.setTextColor(resources.getColor(R.color.green_income, null))
        } else {
            tvContent.setTextColor(resources.getColor(R.color.red_expense, null))
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}