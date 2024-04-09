package com.lbdev.budgetbuzz.ui.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.Transaction
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository
import com.lbdev.budgetbuzz.databinding.FragmentActivityBinding
import com.lbdev.budgetbuzz.ui.adaptor.TransactionsAdaptor
import com.lbdev.budgetbuzz.ui.viewmodel.SharedViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.TransactionsViewModel
import java.util.Calendar
import java.util.Date

class ActivityFragment : Fragment() {
    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!
    private val transactionsRepository = TransactionsRepository()
    private lateinit var transactionsViewModel: TransactionsViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: TransactionsAdaptor
    private var transactions: List<Transaction> = emptyList()
    private var filteredTransactions: List<Transaction> = emptyList()
    private var totalExpense = 0.0
    private var totalIncome = 0.0
    var fromDate = Timestamp(Date(2022 - 1900, 4, 1))
    var toDate = Timestamp.now()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.isTransactionAdded.observe(viewLifecycleOwner) { isAdded ->
            if (isAdded) {
                transactionsViewModel.getUserTransaction()
                sharedViewModel.isTransactionAdded.value = false
            }
        }

        transactionsViewModel.transactions.observe(viewLifecycleOwner) { transactions->
            if (transactions != null) {
                this.transactions = transactions
                filteredTransactions = transactions.filter { it.date in fromDate..toDate }.sortedByDescending { it.date }

                var totalExpenses = 0.0
                var totalIncomes = 0.0
                for (transaction in filteredTransactions) {
                    if (transaction.type == "Expense") {
                        totalExpenses += transaction.amount.toBigDecimal().toDouble()
                    } else {
                        totalIncomes += transaction.amount.toBigDecimal().toDouble()
                    }
                }
                totalExpense = totalExpenses
                totalIncome = totalIncomes
                showTransactions()
            }
        }
    }

    private fun showTransactions() {
        if (transactions.isEmpty())
        {
            binding.transactionsCL.visibility = View.GONE
            binding.noTransactionsLL.visibility = View.VISIBLE
            return
        } else {
            binding.transactionsCL.visibility = View.VISIBLE
            binding.noTransactionsLL.visibility = View.GONE
            if (filteredTransactions.isEmpty()) {
                binding.amountSpentTv.text = "0.0"
                binding.amountEarnedTv.text = "0.0"
                binding.transactionsLL.visibility = View.GONE
                binding.noTransactionsInnerLL.visibility = View.VISIBLE
                return
            } else {
                binding.progressBar.visibility = View.GONE
                binding.amountSpentTv.text = totalExpense.toString()
                binding.amountEarnedTv.text = totalIncome.toString()
                adapter = TransactionsAdaptor(filteredTransactions)
                binding.transactionsRV.layoutManager = LinearLayoutManager(context)
                binding.transactionsRV.setHasFixedSize(true)
                binding.transactionsRV.adapter = adapter
                binding.transactionsCL.visibility = View.VISIBLE
                binding.noTransactionsLL.visibility = View.GONE
                binding.transactionsLL.visibility = View.VISIBLE
                binding.noTransactionsInnerLL.visibility = View.GONE
            }
        }

        if ((totalIncome - totalExpense) < 0) {
            binding.totalTransactionAmountTV.text =
                (totalIncome - totalExpense).toString().replace("-", "")
            binding.totalAmountSignTV.text = getString(R.string.minusSign)
        } else {
            binding.totalTransactionAmountTV.text =
                (totalIncome - totalExpense).toString().replace("+", "")
            binding.totalAmountSignTV.text = getString(R.string.plusSign)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        val view = binding.root
        transactionsViewModel = TransactionsViewModel(transactionsRepository)
        transactionsViewModel.getUserTransaction()

        transactionsViewModel.transactions.observe(viewLifecycleOwner) { transactions->
            if (transactions != null) {
                filteredTransactions = transactions.filter { it.date in fromDate..toDate }.sortedByDescending { it.date }
                var totalExpenses = 0.0
                var totalIncomes = 0.0
                for (transaction in filteredTransactions) {
                    if (transaction.type == "Expense") {
                        totalExpenses += transaction.amount.toBigDecimal().toDouble()
                    } else {
                        totalIncomes += transaction.amount.toBigDecimal().toDouble()
                    }
                }
                totalExpense = totalExpenses
                totalIncome = totalIncomes
                showTransactions()
            }
        }
        return view
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

                fromDate = Timestamp(Date(year - 1900, month, dayOfMonth))
//                val currentDate = "$dayOfMonth ${months[month]} $year"
//                binding.dateTV.text = currentDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}