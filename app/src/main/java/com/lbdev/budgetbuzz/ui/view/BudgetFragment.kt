package com.lbdev.budgetbuzz.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.Budget
import com.lbdev.budgetbuzz.data.repository.BudgetRepository
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository
import com.lbdev.budgetbuzz.databinding.FragmentBudgetBinding
import com.lbdev.budgetbuzz.ui.viewmodel.BudgetViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.SharedViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.TransactionsViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Calendar

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val transactionsRepository = TransactionsRepository()
    private lateinit var transactionsViewModel: TransactionsViewModel
    private val budgetRepository = BudgetRepository()
    private lateinit var budgetViewModel: BudgetViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
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
    private var budgetNotificationPref: SharedPreferences? = null
    private var isBudgetNotificationEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        val view = binding.root
        transactionsViewModel = TransactionsViewModel(transactionsRepository)
        budgetViewModel = BudgetViewModel(budgetRepository)
        transactionsViewModel.getUserTransaction()
        sharedViewModel.isTransactionAdded.observe(viewLifecycleOwner) { isAdded ->
            if (isAdded) {
                transactionsViewModel.getUserTransaction()
                sharedViewModel.isTransactionAdded.value = false
            }
        }
        budgetViewModel.getUserBudget()
        budgetViewModel.budget.observe(viewLifecycleOwner) { budget ->
            if (budget != null) {
                sharedViewModel.userBudget(budget.amount)
                binding.content.visibility = View.VISIBLE
                binding.addBudgetIv.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.note_icon,
                        null
                    )
                )
                binding.budgetNameET.setText(budget.name)
                binding.bdNameTv.text = budget.name
                binding.budgetAmountET.setText(budget.amount)
                binding.budgetNameTv.text = budget.name
                binding.budgetFromTv.text = buildString {
                    append("from ")
                    append(getString(R.string.currencySign))
                    append(budget.amount)
                }
                binding.bdBudgetTv.text = budget.amount
                totalBudget = budget.amount.toInt()
                transactionsViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
                    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val lastDate = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.MONTH, currentMonth)
                        set(Calendar.YEAR, currentYear)
                    }
                    val budgetFromDateTv = buildString {
                        append(calendarMonths[currentMonth])
                        append(" ")
                        append(1)
                        append(",")
                        append(" ")
                        append(currentYear)
                    }
                    binding.budgetFromDateTv.text = budgetFromDateTv
                    binding.bdBudgetFromDateTv.text = budgetFromDateTv

                    val budgetToDateTV = buildString {
                        append(calendarMonths[currentMonth])
                        append(" ")
                        append(lastDate.get(Calendar.DAY_OF_MONTH))
                        append(",")
                        append(" ")
                        append(currentYear)
                    }
                    binding.budgetToDateTv.text = budgetToDateTV
                    binding.bdBudgetToDateTv.text = budgetToDateTV

                    val monthTransactions = transactions.filter {
                        val transactionMonth = Calendar.getInstance().apply {
                            time = it.date.toDate()
                        }.get(Calendar.MONTH)
                        transactionMonth == currentMonth
                    }
                    val expenseTransactions = monthTransactions.filter { it.type == "Expense" }
                    spentBudget =
                        expenseTransactions.filter { it.type == "Expense" }
                            .sumOf { it.amount.toInt() }
                    val spentBudgetText = buildString {
                        append(getString(R.string.currencySign))
                        append(spentBudget)
                    }
                    sharedViewModel.userExpense(spentBudget)
                    binding.budgetSpendTv.text = spentBudgetText
                    binding.bdBudgetSpendTv.text = spentBudgetText

                    binding.budgetProgress.max = 100
                    val progress = ((spentBudget.toFloat() / totalBudget) * 100)
                    binding.budgetProgress.progress = progress.toInt()
                    if (progress > 50) {
                        binding.budgetProgress.progressDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.budget_red_progress_bar,
                            null
                        )
                    } else {
                        binding.budgetProgress.progressDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.budget_green_progress_bar,
                            null
                        )
                    }
                    val spentText = buildString {
                        append(progress)
                        append("%")
                    }
                    binding.progressPercentageTv.text = spentText

                    binding.bdBudgetProgress.max = 100
                    binding.bdBudgetProgress.progress = progress.toInt()
                    if (progress > 50) {
                        binding.bdBudgetProgress.progressDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.budget_red_progress_bar,
                            null
                        )
                    } else {
                        binding.bdBudgetProgress.progressDrawable = ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.budget_green_progress_bar,
                            null
                        )
                    }
                    binding.bdProgressPercentageTv.text = spentText

                    val remainingBudget = totalBudget - spentBudget
                    val remainingDays = lastDate.get(Calendar.DAY_OF_MONTH) - Calendar.getInstance()
                        .get(Calendar.DAY_OF_MONTH) + 1
                    val canSpendPerDay = remainingBudget / remainingDays
                    binding.amountCanSpendTV.text = canSpendPerDay.toString()

                    val avgPerDaySpent =
                        spentBudget / Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    binding.avgSpentAmountTV.text = avgPerDaySpent.toString()

                    val bestDay = expenseTransactions.minByOrNull { it.amount.toInt() }
                    val worstDay = expenseTransactions.maxByOrNull { it.amount.toInt() }
                    binding.bestDayAmountTV.text = bestDay?.amount
                    binding.worstDayAmountTV.text = worstDay?.amount

                    val weekCount = getWeeksInCurrentMonth()
                    for (i in 1..weekCount) {
                        val weekTransactions = expenseTransactions.filter {
                            val transactionWeek = Calendar.getInstance().apply {
                                time = it.date.toDate()
                            }.get(Calendar.WEEK_OF_MONTH)
                            transactionWeek == i
                        }
                        val weekExpenseAmount = weekTransactions.sumOf { it.amount.toInt() }
                        when (i) {
                            1 -> {
                                binding.week1AmountTv.text = weekExpenseAmount.toString()
                                val week1Percent = String.format("%.2f", ((weekExpenseAmount.toFloat() / totalBudget) * 100)).toFloat()
                                binding.week1ProgressBar.max = 100
                                binding.week1ProgressBar.progress = week1Percent.toInt()
                                binding.week1ProgressTv.text = buildString {
                                    append(week1Percent)
                                    append("%")
                                }
                            }

                            2 -> {
                                binding.week2AmountTv.text = weekExpenseAmount.toString()
                                val previousPercent = binding.week1ProgressTv.text.toString().replace("%", "").toFloat()
                                val week2Percent = String.format("%.2f", ((weekExpenseAmount.toFloat() / totalBudget) * 100)).toFloat()
                                binding.week2ProgressBar.max = 100
                                binding.week2ProgressBar.progress = (previousPercent + week2Percent).toInt()
                                binding.week2PreviousTv.text = buildString {
                                    append(previousPercent)
                                    append("% ")
                                }
                                binding.week2CurrentTv.text = buildString {
                                    append("+")
                                    append(" ")
                                    append(week2Percent)
                                    append("% = ")
                                }
                                binding.week2TotalTv.text = buildString {
                                    append(previousPercent + week2Percent)
                                    append("%")
                                }
                            }

                            3 -> {
                                binding.week3AmountTv.text = weekExpenseAmount.toString()
                                val previousPercent = binding.week2TotalTv.text.toString().replace("%", "").toFloat()
                                val week3Percent =
                                    String.format("%.2f", ((weekExpenseAmount.toFloat() / totalBudget) * 100)).toFloat()
                                binding.week3ProgressBar.max = 100
                                binding.week3ProgressBar.progress = (previousPercent + week3Percent).toInt()
                                binding.week3PreviousTv.text = buildString {
                                    append(previousPercent)
                                    append("% ")
                                }
                                binding.week3CurrentTv.text = buildString {
                                    append("+")
                                    append(" ")
                                    append(week3Percent)
                                    append("% = ")
                                }
                                binding.week3TotalTv.text = buildString {
                                    append(previousPercent + week3Percent)
                                    append("%")
                                }
                            }

                            4 -> {
                                binding.week4AmountTv.text = weekExpenseAmount.toString()
                                val previousPercent = binding.week3TotalTv.text.toString().replace("%", "").toFloat()
                                val week4Percent =
                                    String.format("%.2f", ((weekExpenseAmount.toFloat() / totalBudget) * 100)).toFloat()
                                binding.week4ProgressBar.max = 100
                                binding.week4ProgressBar.progress = (previousPercent + week4Percent).toInt()
                                binding.week4PreviousTv.text = buildString {
                                    append(previousPercent)
                                    append("% ")
                                }
                                binding.week4CurrentTv.text = buildString {
                                    append("+")
                                    append(" ")
                                    append(week4Percent)
                                    append("% = ")
                                }
                                binding.week4TotalTv.text = buildString {
                                    append(previousPercent + week4Percent)
                                    append("%")
                                }
                            }

                            5 -> {
                                binding.week5AmountTv.text = weekExpenseAmount.toString()
                                val previousPercent = binding.week4TotalTv.text.toString().replace("%", "").toFloat()
                                val week5Percent =
                                    String.format("%.2f", ((weekExpenseAmount.toFloat() / totalBudget) * 100)).toFloat()
                                binding.week5ProgressBar.max = 100
                                binding.week5ProgressBar.progress = (previousPercent + week5Percent).toInt()
                                binding.week5PreviousTv.text = buildString {
                                    append(previousPercent)
                                    append("% ")
                                }
                                binding.week5CurrentTv.text = buildString {
                                    append("+")
                                    append(" ")
                                    append(week5Percent)
                                    append("% = ")
                                }
                                binding.week5TotalTv.text = buildString {
                                    append(previousPercent + week5Percent)
                                    append("%")
                                }
                            }
                        }
                    }
                }
            }
        }

        backdrop = binding.backdropView
        sheet = binding.content

        binding.addBudgetIv.setOnClickListener {
            toggleBackdrop()
        }

        binding.saveBudgetBtn.setOnClickListener {
            if (binding.budgetNameET.text.isEmpty()) {
                binding.budgetNameET.error = "Please enter a budget name"
                return@setOnClickListener
            }
            if (binding.budgetAmountET.text.isEmpty()) {
                binding.budgetAmountET.error = "Please enter a budget amount"
                return@setOnClickListener
            }
            binding.saveBudgetBtn.isEnabled = false
            binding.addBudgetProgressBar.visibility = View.VISIBLE
            val budgetName = binding.budgetNameET.text.toString()
            val budgetAmount = binding.budgetAmountET.text.toString()
            val budget = Budget(budgetName, budgetAmount)
            if (binding.budgetNotificationSwitch.isChecked) {
                val editor = budgetNotificationPref!!.edit()
                editor.putBoolean("budgetNotificationEnabled", isBudgetNotificationEnabled)
                editor.apply()
            } else {
                val editor = budgetNotificationPref!!.edit()
                editor.putBoolean("budgetNotificationEnabled", isBudgetNotificationEnabled)
                editor.apply()
            }
            budgetViewModel.saveUserBudget(budget)
        }

        budgetViewModel.savedBudget.observe(viewLifecycleOwner) { savedBudget ->
            if (savedBudget == "Added") {
                binding.saveBudgetBtn.isEnabled = true
                binding.addBudgetProgressBar.visibility = View.GONE
                toggleBackdrop()
                Toast.makeText(requireContext(), "Budget Saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.budgetDetailsTv.setOnClickListener {
            binding.saveBudgetView.visibility = View.GONE
            val mSlideRight = Slide()
            mSlideRight.slideEdge = Gravity.END
            TransitionManager.beginDelayedTransition(binding.budgetDetailsCL, mSlideRight)
            binding.budgetDetailsView.visibility = View.VISIBLE
        }

        binding.backToBudget.setOnClickListener {
            binding.budgetDetailsView.visibility = View.GONE
            val mSlideLeft = Slide()
            mSlideLeft.slideEdge = Gravity.START
            TransitionManager.beginDelayedTransition(binding.budgetDetailsCL, mSlideLeft)
            binding.saveBudgetView.visibility = View.VISIBLE
        }

        val budgetNotificationSwitch = binding.budgetNotificationSwitch

        budgetNotificationPref =
            requireActivity().getSharedPreferences("budgetNotificationPref", Context.MODE_PRIVATE)
        isBudgetNotificationEnabled = budgetNotificationPref!!.getBoolean("budgetNotificationEnabled", false)

        if (isBudgetNotificationEnabled) {
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
        budgetNotificationSwitch.isChecked = isBudgetNotificationEnabled

        budgetNotificationSwitch.setOnCheckedChangeListener { _, b ->
            isBudgetNotificationEnabled = b
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
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)

            budgetViewModel.getUserBudget()
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

    private fun getWeeksInCurrentMonth(): Int {
        val currentDate = LocalDate.now()
        val firstDayOfMonth = currentDate.with(TemporalAdjusters.firstDayOfMonth())
        val lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth())

        var weekCount = 0
        var date = firstDayOfMonth

        while (!date.isAfter(lastDayOfMonth)) {
            if (date.dayOfWeek == DayOfWeek.MONDAY) {
                weekCount++
            }
            date = date.plusDays(1)
        }

        return weekCount
    }
}