package com.lbdev.budgetbuzz.ui.adaptor

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lbdev.budgetbuzz.ui.view.ExpenseFragment
import com.lbdev.budgetbuzz.ui.view.IncomeFragment

class PagerAdapter(fragmentActivity: FragmentActivity,private val listSize: Int,private val fragment: Fragment) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        val itemsPerPage = 9
        return (listSize + itemsPerPage - 1) / itemsPerPage
    }

    override fun createFragment(position: Int): Fragment {
        val itemsPerPage = 9
        val startIndex = position * itemsPerPage
        val count = if (position == itemCount - 1) {
            listSize - startIndex
        } else {
            itemsPerPage
        }

        return if (fragment is ExpenseFragment) {
            ExpenseFragment.newInstance(startIndex, count)
        } else {
            IncomeFragment.newInstance(startIndex, count)
        }
    }
}
