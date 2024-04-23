package com.lbdev.budgetbuzz.ui.view

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
import ru.cleverpumpkin.calendar.CalendarDate
import java.time.LocalDate
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

        binding.filterIV.setOnClickListener {
            val oldestDate = transactions.minByOrNull { it.date }?.date
            val newestDate = transactions.maxByOrNull { it.date }?.date
            val minDate = oldestDate?.let { CalendarDate(it.toDate()) }
            val maxDate = newestDate?.let { CalendarDate(it.toDate()) }

            val calendarFragment = CalendarFragment(minDate, maxDate)
            calendarFragment.onDateSelectedListener = object : CalendarFragment.OnDateSelectedListener {
                override fun onDateSelected(startDate: LocalDate, endDate: LocalDate) {
                    fromDate = Timestamp(Date(startDate.year - 1900, startDate.monthValue - 1, startDate.dayOfMonth))
                    toDate = Timestamp(Date(endDate.year - 1900, endDate.monthValue - 1, endDate.dayOfMonth))
                    transactionsViewModel.getUserTransaction()
                }
            }

            val transaction = parentFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            transaction.replace(android.R.id.content, calendarFragment)
            transaction.addToBackStack(null)
            transaction.commit()
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
        return view
    }

    private fun showTransactions() {
        if (transactions.isEmpty())
        {
            binding.progressBar.visibility = View.GONE
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
            binding.netTextView.text = getString(R.string.deficit_of)
            binding.totalTransactionAmountTV.text =
                (totalIncome - totalExpense).toString().replace("-", "")
            binding.totalAmountSignTV.text = getString(R.string.minusSign)
        } else {
            binding.netTextView.text = getString(R.string.net_earning)
            binding.totalTransactionAmountTV.text =
                (totalIncome - totalExpense).toString().replace("+", "")
            binding.totalAmountSignTV.text = getString(R.string.plusSign)
        }
    }
}