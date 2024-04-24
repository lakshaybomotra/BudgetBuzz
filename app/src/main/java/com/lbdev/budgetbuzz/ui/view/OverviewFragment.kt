package com.lbdev.budgetbuzz.ui.view

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.Transaction
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository
import com.lbdev.budgetbuzz.databinding.FragmentOverviewBinding
import com.lbdev.budgetbuzz.ui.viewmodel.SharedViewModel
import com.lbdev.budgetbuzz.ui.viewmodel.TransactionsViewModel
import com.lbdev.budgetbuzz.util.CustomMarkerView
import java.text.DecimalFormat
import java.util.Calendar

class OverviewFragment : Fragment() {
    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!
    private val transactionsRepository = TransactionsRepository()
    private lateinit var transactionsViewModel: TransactionsViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var expensePieChart: PieChart
    private lateinit var incomePieChart: PieChart
    private lateinit var lineChart: LineChart
    private var thisMonthExpenses = 0
    private var thisMonthIncomes = 0
    lateinit var currencySign: String
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private val calendarMonths = arrayOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    )

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

        binding.calendarTab.setOnClickListener {
            showMonthYearPicker()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        val view = binding.root
        currencySign = resources.getString(R.string.currencySign)
        transactionsViewModel = TransactionsViewModel(transactionsRepository)
        transactionsViewModel.getUserTransaction()
        binding.selectedMonthYearTV.text = "${calendarMonths[selectedMonth]} $selectedYear"
        transactionsViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val monthName = calendarMonths[selectedMonth]
            val monthTransactions =
                transactions.filter {
                    it.date.toDate().toString().split(" ")[1] == monthName && it.date.toDate()
                        .toString().split(" ")[5] == selectedYear.toString()
                }
            val expenseTransactions = monthTransactions.filter { it.type == "Expense" }
            val incomeTransactions = monthTransactions.filter { it.type == "Income" }

            thisMonthExpenses = expenseTransactions.sumOf { it.amount.toInt() }
            thisMonthIncomes = incomeTransactions.sumOf { it.amount.toInt() }

            binding.amountSpentTv.text = thisMonthExpenses.toString()
            binding.amountEarnedTv.text = thisMonthIncomes.toString()

            transactionsLineChart(monthTransactions)
            expenseChart(expenseTransactions)
            incomeChart(incomeTransactions)
        }
        expensePieChart = binding.expensePieChart
        incomePieChart = binding.incomePieChart
        lineChart = binding.chartLine

        return view
    }

    class PercentFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return String.format("%.1f %%", value)
        }
    }

    private fun transactionsLineChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("No data available for selected month")
            lineChart.invalidate()
            return
        }

        val markerView = CustomMarkerView(requireContext(), R.layout.marker_view)
        lineChart.marker = markerView
        lineChart.isHighlightPerTapEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(false)
        lineChart.setScaleEnabled(false)
        lineChart.description.isEnabled = false
        lineChart.setExtraOffsets(5f, 10f, 5f, 5f)
        lineChart.animateX(1000)
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.grey_graph)
        lineChart.axisRight.setDrawGridLines(false)
        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.setDrawAxisLine(false)
        lineChart.xAxis.setDrawAxisLine(false)
        lineChart.legend.isEnabled = false
        lineChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.text_heading)
        lineChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_heading)
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.valueFormatter = KValueFormatter()
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        yAxisLeft.granularity = 1f
        yAxisLeft.isGranularityEnabled = true
        xAxis.isGranularityEnabled = true
        lineChart.invalidate()

        val lineEntries = ArrayList<Entry>()
        val sortedTransactions = transactions.sortedBy { it.date.toDate().toString().split(" ")[2] }
        sortedTransactions.forEachIndexed { _, transaction ->
            val date = transaction.date.toDate().toString().split(" ")[2]
            val amount =
                if (transaction.type == "Expense") -transaction.amount.toFloat() else transaction.amount.toFloat()
            lineEntries.add(Entry(date.toFloat(), amount))
        }

        val lineChartSet = LineDataSet(lineEntries, "")
        lineChartSet.setDrawFilled(true)
        lineChartSet.fillColor = ContextCompat.getColor(requireContext(), R.color.fill_graph)
        lineChartSet.valueFormatter = KValueFormatter()
        lineChartSet.color = ContextCompat.getColor(requireContext(), R.color.blue_graph)
        lineChartSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_heading)
        lineChartSet.valueTextSize = 9f
        lineChartSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lineChartSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue_graph))
        lineChartSet.lineWidth = 2f
        lineChartSet.circleRadius = 6f
        lineChartSet.setDrawCircleHole(true)
        lineChart.data = LineData(lineChartSet)
        lineChart.invalidate()
    }

    private fun expenseChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            expensePieChart.clear()
            expensePieChart.setNoDataText("No data available for selected month")
            expensePieChart.invalidate()
            return
        }

        expensePieChart.setUsePercentValues(true)
        expensePieChart.description.isEnabled = false
        expensePieChart.setExtraOffsets(5f, 10f, 5f, 20f)
        expensePieChart.dragDecelerationFrictionCoef = 0.95f
        expensePieChart.isDrawHoleEnabled = true
        expensePieChart.transparentCircleRadius = 81f
        expensePieChart.setDrawEntryLabels(false)
        expensePieChart.setDrawCenterText(true)
        val totalExpenses = transactions.sumOf { it.amount.toBigDecimal() }
        val centerText = SpannableString("Total Expenses\n-${currencySign}$totalExpenses")
        centerText.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.text_heading, null)),
            0,
            "Total Expenses".length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        centerText.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.red_expense, null)),
            "Total Expenses".length,
            centerText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        expensePieChart.centerText = centerText
        expensePieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD)
        expensePieChart.setCenterTextSize(18f)
        expensePieChart.setCenterTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_heading
            )
        )
        expensePieChart.holeRadius = 81f
        expensePieChart.setHoleColor(resources.getColor(R.color.card_background, null))
        expensePieChart.setTouchEnabled(true)
        expensePieChart.animateY(2000)

        val categories = transactions.groupBy { it.category }
            .mapValues { (_, transactions) ->
                String.format("%.2f", transactions.sumOf { it.amount.toBigDecimal().toDouble() })
                    .toDouble()
            }

        val totalAmount = categories.values.sum()
        val entries = categories.map { (category, amount) ->
            val percentage = (amount / totalAmount) * 100
            PieEntry(percentage.toFloat(), category.name)
        }

        val expenseChartLegend = expensePieChart.legend
        expenseChartLegend.isEnabled = true
        expenseChartLegend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        expenseChartLegend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        expenseChartLegend.orientation = Legend.LegendOrientation.VERTICAL
        expenseChartLegend.setDrawInside(true)
        expenseChartLegend.xEntrySpace = 7f
        expenseChartLegend.yEntrySpace = 0f
        expenseChartLegend.yOffset = 0f
        expenseChartLegend.textSize = 12f
        expenseChartLegend.textColor = resources.getColor(R.color.text_heading, null)

        val expenseDataSet = PieDataSet(entries, "")
        expenseDataSet.valueLinePart1OffsetPercentage = 80f
        expenseDataSet.valueLinePart1Length = 0.4f
        expenseDataSet.valueLinePart2Length = 0.3f
        expenseDataSet.valueLineColor = resources.getColor(R.color.text_heading, null)
        expenseDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        expenseDataSet.valueTextColor = resources.getColor(R.color.text_heading, null)
        expenseDataSet.valueTextSize = 12f
        expenseDataSet.sliceSpace = 2f
        expenseDataSet.selectionShift = 5f
        expenseDataSet.valueFormatter = PercentFormatter()
        val colors = ArrayList<Int>()
        categories.keys.forEach { category ->
            colors.add(Color.parseColor(category.startColor))
        }
        expenseDataSet.colors = colors

        val data = PieData(expenseDataSet)
        expensePieChart.data = data
        expensePieChart.invalidate()
    }

    private fun incomeChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            incomePieChart.clear()
            incomePieChart.setNoDataText("No data available for selected month")
            incomePieChart.invalidate()
            return
        }

        incomePieChart.setUsePercentValues(true)
        incomePieChart.description.isEnabled = false
        incomePieChart.setExtraOffsets(5f, 10f, 5f, 20f)
        incomePieChart.dragDecelerationFrictionCoef = 0.95f
        incomePieChart.isDrawHoleEnabled = true
        incomePieChart.transparentCircleRadius = 81f
        incomePieChart.setDrawEntryLabels(false)
        incomePieChart.setDrawCenterText(true)
        val totalIncomes = transactions.sumOf { it.amount.toBigDecimal() }
        val centerText = SpannableString("Total Incomes\n+${currencySign}$totalIncomes")
        centerText.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.text_heading, null)),
            0,
            "Total Incomes".length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        centerText.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.green_income, null)),
            "Total Incomes".length,
            centerText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        incomePieChart.centerText = centerText
        incomePieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD)
        incomePieChart.setCenterTextSize(18f)
        incomePieChart.setCenterTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_heading
            )
        )
        incomePieChart.holeRadius = 81f
        incomePieChart.setHoleColor(resources.getColor(R.color.card_background, null))
        incomePieChart.setTouchEnabled(true)
        incomePieChart.animateY(2000)

        val categories = transactions.groupBy { it.category }
            .mapValues { (_, transactions) ->
                String.format("%.2f", transactions.sumOf { it.amount.toBigDecimal().toDouble() })
                    .toDouble()
            }

        val totalAmount = categories.values.sum()
        val entries = categories.map { (category, amount) ->
            val percentage = (amount / totalAmount) * 100
            PieEntry(percentage.toFloat(), category.name)
        }

        val incomeChartLegend = incomePieChart.legend
        incomeChartLegend.isEnabled = true
        incomeChartLegend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        incomeChartLegend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        incomeChartLegend.orientation = Legend.LegendOrientation.VERTICAL
        incomeChartLegend.setDrawInside(true)
        incomeChartLegend.xEntrySpace = 7f
        incomeChartLegend.yEntrySpace = 0f
        incomeChartLegend.yOffset = 0f
        incomeChartLegend.textSize = 12f
        incomeChartLegend.textColor = resources.getColor(R.color.text_heading, null)

        val incomeDataSet = PieDataSet(entries, "")
        incomeDataSet.valueLinePart1OffsetPercentage = 80f
        incomeDataSet.valueLinePart1Length = 0.4f
        incomeDataSet.valueLinePart2Length = 0.3f
        incomeDataSet.valueLineColor = resources.getColor(R.color.text_heading, null)
        incomeDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        incomeDataSet.valueTextColor = resources.getColor(R.color.text_heading, null)
        incomeDataSet.valueTextSize = 12f
        incomeDataSet.sliceSpace = 2f
        incomeDataSet.selectionShift = 5f
        incomeDataSet.valueFormatter = PercentFormatter()
        val colors = ArrayList<Int>()
        categories.keys.forEach { category ->
            colors.add(Color.parseColor(category.startColor))
        }
        incomeDataSet.colors = colors

        val data = PieData(incomeDataSet)
        incomePieChart.data = data
        incomePieChart.invalidate()
    }

    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            R.style.DatePickerDialogTheme,
            { _, year, month, _ ->
                selectedMonth = month
                selectedYear = year
                binding.selectedMonthYearTV.text = "${calendarMonths[month]} $year"
                transactionsViewModel.getUserTransaction()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.findViewById<View>(
            resources.getIdentifier(
                "day",
                "id",
                "android"
            )
        ).visibility = View.GONE
        dialog.show()
    }

    class KValueFormatter : ValueFormatter() {
        private val decimalFormat = DecimalFormat("#.#")

        override fun getFormattedValue(value: Float): String {
            return if (value >= 1000 || value <= -1000) {
                "${decimalFormat.format(value / 1000)}k"
            } else {
                value.toString()
            }
        }
    }
}