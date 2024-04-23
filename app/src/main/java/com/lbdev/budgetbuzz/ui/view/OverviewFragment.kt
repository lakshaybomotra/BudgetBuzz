package com.lbdev.budgetbuzz.ui.view

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import coil.ImageLoader
import coil.request.ImageRequest
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.Transaction
import com.lbdev.budgetbuzz.data.repository.TransactionsRepository
import com.lbdev.budgetbuzz.databinding.FragmentOverviewBinding
import com.lbdev.budgetbuzz.ui.viewmodel.TransactionsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverviewFragment : Fragment() {
    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!
    private val transactionsRepository = TransactionsRepository()
    private lateinit var transactionsViewModel: TransactionsViewModel
    lateinit var pieChart: PieChart
    lateinit var lineChart: LineChart
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        val view = binding.root

        transactionsViewModel = TransactionsViewModel(transactionsRepository)
        transactionsViewModel.getUserTransaction()

        transactionsViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            //filter the expense transactions
            val expenseTransactions = transactions.filter { it.type == "Expense" }

            GlobalScope.launch(Dispatchers.Main) {
                updateChart(transactions)
            }
        }
        pieChart = binding.expensePieChart
        lineChart = binding.chartLine

        return view
    }

    suspend fun updateChart(transactions: List<Transaction>) {

        class KValueFormatter : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value >= 1000) {
                    "${value / 1000}k"
                } else {
                    value.toString()
                }
            }
        }

        lineChart.setTouchEnabled(false)
        lineChart.setPinchZoom(true)
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
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val yAxisLeft = lineChart.axisLeft
        lineChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.text_heading)
        lineChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_heading)
        yAxisLeft.valueFormatter = KValueFormatter()
        lineChart.invalidate()

        val lineEntries = ArrayList<Entry>()
        val sortedTransactions = transactions.sortedBy { it.date.toDate().toString().split(" ")[2] }
        sortedTransactions.forEachIndexed { index, transaction ->
            val date = transaction.date.toDate().toString().split(" ")[2]
            lineEntries.add(Entry(date.toFloat(), transaction.amount.toFloat()))
        }

        val set1 = LineDataSet(lineEntries, "")
        set1.setDrawFilled(true)
        set1.fillColor = ContextCompat.getColor(requireContext(), R.color.fill_graph)
        set1.valueFormatter = KValueFormatter()
        lineChart.legend.isEnabled = false
        set1.color = ContextCompat.getColor(requireContext(), R.color.blue_graph)
        set1.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_heading)
        set1.valueTextSize = 9f
        set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        set1.setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue_graph))
        set1.lineWidth = 2f
        set1.circleRadius = 6f
        set1.setDrawCircleHole(true)
        lineChart.data = LineData(set1)
        lineChart.invalidate()


        //pie
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(20f, 20f, 20f, 20f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.transparentCircleRadius = 60f
        pieChart.setDrawEntryLabels(false)
        pieChart.setDrawCenterText(false)
        pieChart.holeRadius = 60f
        pieChart.setDrawEntryLabels(false)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.green_income))
        pieChart.setTouchEnabled(true)
        pieChart.animateY(1000)

//        val legend = pieChart.legend
//        legend.isEnabled = true
//        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
//        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
//        legend.orientation = Legend.LegendOrientation.VERTICAL
//        legend.setDrawInside(true)
//        legend.xEntrySpace = 7f
//        legend.yEntrySpace = 0f
//        legend.yOffset = 0f

        // Categorize the transactions
        val categories = transactions.groupBy { it.category }
            .mapValues { (_, transactions) ->
                String.format("%.2f", transactions.sumOf { it.amount.toBigDecimal().toDouble() }).toDouble()
            }

        val totalAmount = categories.values.sum()
        val entries = categories.map { (category, amount) ->
            val percentage = (amount / totalAmount) * 100
            val icon = withContext(Dispatchers.IO) {
                val request = ImageRequest.Builder(requireContext())
                    .data(category.icon)
                    .build()
                val result = ImageLoader(requireContext()).execute(request).drawable
                BitmapDrawable(resources, result?.toBitmap())
            }
            PieEntry(percentage.toFloat(), category.name, icon)
        }

        // Create the PieDataSet
        val dataSet = PieDataSet(entries, "Categories")
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.3f
        dataSet.valueLinePart2Length = 0.2f
        dataSet.valueLineColor = Color.BLACK
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        val colors = ArrayList<Int>()
        //use same color for same category get the color from category object
        categories.keys.forEach { category ->
            colors.add(Color.parseColor(category.startColor))
        }
        dataSet.colors = colors
        dataSet.setDrawIcons(true)
        dataSet.iconsOffset = MPPointF(4f, 50f)

        // Create the PieData and set it to the chart
        val data = PieData(dataSet)
        // ... (set the properties of the data)
        pieChart.data = data
        pieChart.invalidate()
    }
}