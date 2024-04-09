package com.lbdev.budgetbuzz.ui.view

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import coil.decode.SvgDecoder
import coil.load
import com.google.firebase.Timestamp
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.Category
import com.lbdev.budgetbuzz.data.model.Transaction
import com.lbdev.budgetbuzz.data.repository.CategoriesRepository
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository
import com.lbdev.budgetbuzz.databinding.FragmentAddTransactionBinding
import com.lbdev.budgetbuzz.ui.adaptor.PagerAdapter
import com.lbdev.budgetbuzz.ui.viewmodel.CategoryViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.SharedViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.TransactionsViewModel
import java.util.Calendar

class AddTransactionFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var pageIndicator: LinearLayout
    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var transactionsRepository: TransactionsRepository
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryRepository: CategoriesRepository
    lateinit var expenseDate: Timestamp
    private val months = arrayOf(
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var selectedItem = Category()

        sharedViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            if (item.name == "Select a category") {
                selectedItem = item
                binding.categoryTV.text = item.name
                binding.addTransactionAppBar.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.add_transaction_app_bar
                    )
                )
            } else {
                selectedItem = item
                binding.categoryTV.text = item.name
                val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    intArrayOf(Color.parseColor(item.startColor), Color.parseColor(item.endColor))
                )
                binding.addTransactionAppBar.background = gradientDrawable
                binding.closeBtn.setTextColor(resources.getColor(R.color.white, null))
                binding.categoryTV.setTextColor(resources.getColor(R.color.white, null))
                binding.amountET.setTextColor(resources.getColor(R.color.white, null))
                binding.amountET.setHintTextColor(resources.getColor(R.color.white, null))
                binding.amountET.setHintTextColor(binding.amountET.hintTextColors.withAlpha(128))
                binding.currencySignTV.setTextColor(resources.getColor(R.color.white, null))
                binding.operatorSign.setTextColor(resources.getColor(R.color.white, null))
            }
        }

        val viewPager = binding.viewPager

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        sharedViewModel.expenseCategories.observe(viewLifecycleOwner) { list ->
            val adapter = PagerAdapter(requireActivity(), list.size, ExpenseFragment())
            viewPager.adapter = adapter
            removeIndicator()
            setupIndicators(list.size / 9 + 1)
        }

        binding.expenseButton.setOnClickListener {
            binding.expenseButton.strokeWidth = 0
            binding.incomeButton.strokeWidth = 1
            binding.operatorSign.visibility = View.VISIBLE
            binding.operatorSign.text = "-"
            binding.closeBtn.setTextColor(resources.getColor(R.color.text_heading, null))
            binding.categoryTV.setTextColor(resources.getColor(R.color.text_heading, null))
            binding.amountET.setTextColor(resources.getColor(R.color.text_heading, null))
            binding.amountET.setHintTextColor(resources.getColor(R.color.text_heading, null))
            binding.amountET.setHintTextColor(binding.amountET.hintTextColors.withAlpha(128))
            binding.currencySignTV.setTextColor(resources.getColor(R.color.text_heading, null))
            binding.operatorSign.setTextColor(resources.getColor(R.color.text_heading, null))

            binding.incomeButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.activity_background
                )
            )
            binding.incomeText.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.text_heading
                )
            )

            sharedViewModel.removeSelectedItem()

            binding.expenseButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.expense_button
                )
            )
            binding.expenseText.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            sharedViewModel.expenseCategories.observe(viewLifecycleOwner) { list ->
                val adapter = PagerAdapter(requireActivity(), list.size, ExpenseFragment())
                viewPager.adapter = adapter
                removeIndicator()
                setupIndicators(list.size / 9 + 1)
            }
        }

        binding.incomeButton.setOnClickListener {
            binding.expenseButton.strokeWidth = 1
            binding.incomeButton.strokeWidth = 0
            binding.operatorSign.visibility = View.VISIBLE
            binding.operatorSign.text = "+"
            binding.closeBtn.setTextColor(resources.getColor(R.color.text_heading, null))
            binding.categoryTV.setTextColor(resources.getColor(R.color.text_heading, null))
            binding.amountET.setTextColor(resources.getColor(R.color.text_heading, null))
            binding.amountET.setHintTextColor(resources.getColor(R.color.text_heading, null))
            binding.amountET.setHintTextColor(binding.amountET.hintTextColors.withAlpha(128))
            binding.currencySignTV.setTextColor(resources.getColor(R.color.text_heading, null))
            binding.operatorSign.setTextColor(resources.getColor(R.color.text_heading, null))

            binding.expenseButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.activity_background
                )
            )
            binding.expenseText.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.text_heading
                )
            )
            sharedViewModel.removeSelectedItem()
            binding.incomeButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.income_button
                )
            )
            binding.incomeText.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            sharedViewModel.incomeCategories.observe(viewLifecycleOwner) { list ->
                Log.e("lakshay add", "onViewCreated: " + list[0].name)
                val adapter = PagerAdapter(requireActivity(), list.size, IncomeFragment())
                viewPager.adapter = adapter
                removeIndicator()
                setupIndicators(list.size / 9 + 1)
            }
        }

        binding.closeBtn.setOnClickListener {
            sharedViewModel.removeSelectedItem()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            transaction.remove(this)
            transaction.commit()
        }

        binding.transactionContinueBtn.setOnClickListener {
            if (sharedViewModel.selectedItem.value!!.type.isEmpty()) {
                Toast.makeText(
                    requireContext(), "Select Category First", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            sharedViewModel.selectedItem.observe(viewLifecycleOwner) { item ->
                binding.selectedCategoryTV.text = item.name
                val color = Color.parseColor(item.startColor)
                binding.categoryIconIV.load(item.icon) {
                    decoderFactory { result, options, _ -> SvgDecoder(result.source, options) }
                }
                binding.categoryIconIV.setColorFilter(color)
                binding.calendarIconIV.setColorFilter(color)
                binding.noteIconIV.setColorFilter(color)
            }
            binding.transactionContinueBtn.visibility = View.INVISIBLE
            binding.transactionCategoryCard.visibility = View.INVISIBLE
            val mSlideRight = Slide()
            mSlideRight.slideEdge = Gravity.END
            TransitionManager.beginDelayedTransition(binding.transactionConstraintMain, mSlideRight)
            binding.transactionDetailsCard.visibility = View.VISIBLE
            binding.transactionSubmitBtn.visibility = View.VISIBLE
        }

        binding.dateTV.setOnClickListener {
            showDatePicker()
        }

        binding.selectedCategoryTV.setOnClickListener {
            binding.transactionDetailsCard.visibility = View.INVISIBLE
            binding.transactionSubmitBtn.visibility = View.INVISIBLE
            val mSlideLeft = Slide()
            mSlideLeft.slideEdge = Gravity.START
            TransitionManager.beginDelayedTransition(binding.transactionConstraintMain, mSlideLeft)
            binding.transactionCategoryCard.visibility = View.VISIBLE
            binding.transactionContinueBtn.visibility = View.VISIBLE
        }

        binding.transactionSubmitBtn.setOnClickListener {
            if (binding.amountET.text.isNullOrEmpty()) {
                binding.amountET.requestFocus()
                binding.amountET.error = "Enter Amount"
                return@setOnClickListener
            }
            binding.transactionSubmitBtn.isEnabled = false

            if (selectedItem.type == "Expense") {
                val expense = Transaction(
                    sharedViewModel.expenseCategories.value?.find { category -> category.name == selectedItem.name }!!,
                    binding.amountET.text.toString(),
                    expenseDate,
                    binding.transactionNote.text.toString(),
                    "Expense"
                )

                transactionsViewModel.saveUserTransaction(expense)
            } else {
                val income = Transaction(
                    sharedViewModel.incomeCategories.value?.find { category -> category.name == selectedItem.name }!!,
                    binding.amountET.text.toString(),
                    expenseDate,
                    binding.transactionNote.text.toString(),
                    "Income"
                )

                transactionsViewModel.saveUserTransaction(income)
            }


            transactionsViewModel.savedTransaction.observe(viewLifecycleOwner) {
                binding.transactionSubmitBtn.isEnabled = true

                sharedViewModel.isTransactionAdded.value = true

                Toast.makeText(
                    requireContext(), "Transaction Saved", Toast.LENGTH_SHORT
                ).show()
                val transaction = parentFragmentManager.beginTransaction()
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                transaction.remove(this)
                transaction.commit()
            }

            transactionsViewModel.error.observe(viewLifecycleOwner) { error ->
                binding.transactionSubmitBtn.isEnabled = true
                Toast.makeText(
                    requireContext(), error, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        val view = binding.root
        transactionsRepository = TransactionsRepository()
        transactionsViewModel = TransactionsViewModel(transactionsRepository)
        categoryRepository = CategoriesRepository()
        categoryViewModel = CategoryViewModel(categoryRepository)
        sharedViewModel.removeSelectedItem()
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        expenseDate = Timestamp(java.util.Date(year - 1900, month, day))
        val currentDate = "$day ${months[month]} $year"
        binding.dateTV.text = currentDate
        binding.amountET.requestFocus()
        pageIndicator = binding.categoryPageIndicator
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setupIndicators(size: Int) {
        val indicators = arrayOfNulls<ImageView>(size)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(20, 20)
        layoutParams.setMargins(24, 8, 12, 8)

        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.indicator_inactive
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
                        requireContext(), R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    private fun removeIndicator() {
        pageIndicator.removeAllViews()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
//                 val selectedDateTimestamp = Timestamp(
//                    java.util.Date(
//                        year - 1900,
//                        month,
//                        dayOfMonth
//                    ))

//                if (selectedDateTimestamp.toDate()>fromDateTimestamp.toDate() && selectedDateTimestamp.toDate()<toDateTimestamp.toDate()) {
//                    Toast.makeText(
//                        requireContext(), "In range", Toast.LENGTH_SHORT
//                    ).show()
//                    return@DatePickerDialog
//                }

                expenseDate = Timestamp(java.util.Date(year - 1900, month, dayOfMonth))
                val currentDate = "$dayOfMonth ${months[month]} $year"
                binding.dateTV.text = currentDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}