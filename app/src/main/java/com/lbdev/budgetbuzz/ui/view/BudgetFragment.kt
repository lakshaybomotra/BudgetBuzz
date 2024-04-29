package com.lbdev.budgetbuzz.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository
import com.lbdev.budgetbuzz.databinding.FragmentBudgetBinding
import com.lbdev.budgetbuzz.ui.viewmodel.TransactionsViewModel
import java.util.Calendar

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val transactionsRepository = TransactionsRepository()
    private lateinit var transactionsViewModel: TransactionsViewModel
    lateinit var backdrop: View
    lateinit var sheet: View
    private var spentBudget = 0
    private var totalBudget = 10000
    private val calendarMonths = arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        val view = binding.root
        transactionsViewModel = TransactionsViewModel(transactionsRepository)
        transactionsViewModel.getUserTransaction()
        transactionsViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val lastDate = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.MONTH, currentMonth)
                set(Calendar.YEAR, currentYear)
            }
            binding.budgetFromDateTv.text = buildString {
                append(calendarMonths[currentMonth])
                append(" ")
                append(1)
                append(",")
                append(" ")
                append(currentYear)
            }
            binding.budgetToDateTv.text = buildString {
                append(calendarMonths[currentMonth])
                append(" ")
                append(lastDate.get(Calendar.DAY_OF_MONTH))
                append(",")
                append(" ")
                append(currentYear)
            }
            val monthTransactions = transactions.filter {
                val transactionMonth = Calendar.getInstance().apply {
                    time = it.date.toDate()
                }.get(Calendar.MONTH)
                transactionMonth == currentMonth
            }
            val expenseTransactions = monthTransactions.filter { it.type == "Expense" }
            spentBudget =
                expenseTransactions.filter { it.type == "Expense" }.sumOf { it.amount.toInt() }
            binding.budgetSpendTv.text = buildString {
                append(getString(R.string.currencySign))
                append(spentBudget)
            }

            binding.budgetFromTv.text = buildString {
                append("from ")
                append(getString(R.string.currencySign))
                append(totalBudget)
            }

            val greenProgress =
                ResourcesCompat.getDrawable(resources, R.drawable.budget_green_progress_bar, null)
            val redProgress =
                ResourcesCompat.getDrawable(resources, R.drawable.budget_red_progress_bar, null)

            binding.budgetProgress.max = 100
            val progress = ((spentBudget.toFloat() / totalBudget) * 100).toInt()
            binding.budgetProgress.progress = progress
            if (progress > 50) {
                binding.budgetProgress.progressDrawable = redProgress
            } else {
                binding.budgetProgress.progressDrawable = greenProgress
            }
            binding.progressPercentageTv.text = buildString {
                append(binding.budgetProgress.progress)
                append("%")
            }
        }
        backdrop = binding.backdropView
        sheet = binding.content

        binding.addBudgetIv.setOnClickListener {
            toggleBackdrop()
        }

        val budgetNotificationSwitch = binding.budgetNotificationSwitch
        budgetNotificationSwitch.setOnCheckedChangeListener { _, b ->
            if (b) {
                budgetNotificationSwitch.trackDecorationTintList =
                    resources.getColorStateList(R.color.green_income, null)
                budgetNotificationSwitch.trackTintList =
                    resources.getColorStateList(R.color.green_income, null)
            } else {
                budgetNotificationSwitch.trackDecorationTintList =
                    resources.getColorStateList(R.color.red_expense, null)
                budgetNotificationSwitch.trackTintList =
                    resources.getColorStateList(R.color.red_expense, null)
            }
        }

        return view
    }

    private fun toggleBackdrop() {
        val isBackdropVisible = backdrop.visibility == View.VISIBLE

        if (isBackdropVisible) {
            binding.addBudgetIv.rotation = 0f
            activity?.window?.statusBarColor =
                ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
            binding.forBackdropLL.background =
                ResourcesCompat.getDrawable(resources, R.color.card_background, null)
            val fadeOut = ObjectAnimator.ofFloat(backdrop, "alpha", 1f, 0f)
            fadeOut.duration = 300
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    backdrop.visibility = View.GONE
                }
            })
            fadeOut.start()
            val moveSheetUp = ObjectAnimator.ofFloat(sheet, "translationY", 0f, 1f)
            moveSheetUp.duration = 300
            moveSheetUp.start()

        } else {
            binding.addBudgetIv.rotation = 45f
            activity?.window?.statusBarColor =
                ResourcesCompat.getColor(resources, R.color.green_main, null)
            binding.forBackdropLL.background =
                ResourcesCompat.getDrawable(resources, R.drawable.green_gradient_square, null)
            val fadeIn = ObjectAnimator.ofFloat(backdrop, "alpha", 0f, 1f)
            fadeIn.duration = 300
            fadeIn.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    backdrop.visibility = View.VISIBLE
                }
            })
            fadeIn.start()
            val moveSheetDown =
                ObjectAnimator.ofFloat(sheet, "translationY", -backdrop.height.toFloat(), 0f)
            moveSheetDown.duration = 300
            moveSheetDown.start()
        }
    }
}