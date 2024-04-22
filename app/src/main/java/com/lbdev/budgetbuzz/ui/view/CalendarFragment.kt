package com.lbdev.budgetbuzz.ui.view

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.databinding.CalendarDayBinding
import com.lbdev.budgetbuzz.databinding.CalendarHeaderBinding
import com.lbdev.budgetbuzz.databinding.FragmentCalendarBinding
import com.lbdev.budgetbuzz.ui.base.BaseFragment
import com.lbdev.budgetbuzz.ui.base.HasBackButton
import com.lbdev.budgetbuzz.ui.base.HasToolbar
import com.lbdev.budgetbuzz.util.ContinuousSelectionHelper.getSelection
import com.lbdev.budgetbuzz.util.ContinuousSelectionHelper.isInDateBetweenSelection
import com.lbdev.budgetbuzz.util.ContinuousSelectionHelper.isOutDateBetweenSelection
import com.lbdev.budgetbuzz.util.DateSelection
import com.lbdev.budgetbuzz.util.displayText
import com.lbdev.budgetbuzz.util.getColorCompat
import com.lbdev.budgetbuzz.util.getDrawableCompat
import com.lbdev.budgetbuzz.util.makeInVisible
import com.lbdev.budgetbuzz.util.makeVisible
import com.lbdev.budgetbuzz.util.setTextColorRes
import ru.cleverpumpkin.calendar.CalendarDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment(
    val firstTransactionDate: CalendarDate?,
    val lastTransactionDate: CalendarDate?
) : BaseFragment(R.layout.fragment_calendar), HasToolbar, HasBackButton {
    override val toolbar: Toolbar
        get() = binding.exFourToolbar

    override val titleRes: Int? = null

    private val today = LocalDate.now()

    private var selection = DateSelection()

    private val headerDateFormatter = DateTimeFormatter.ofPattern("EEE'\n'd MMM")

    private lateinit var binding: FragmentCalendarBinding
    var onDateSelectedListener: OnDateSelectedListener? = null

    val startMonth = YearMonth.of(
        firstTransactionDate!!.year,
        firstTransactionDate.month+1
    )

    val endMonth = YearMonth.now()
    val firstDate = LocalDate.of(
        firstTransactionDate!!.year,
        firstTransactionDate.month+1,
        firstTransactionDate.dayOfMonth
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        addStatusBarColorUpdate(R.color.white)
        setHasOptionsMenu(true)
        binding = FragmentCalendarBinding.bind(view)
        // Set the First day of week depending on Locale
        val daysOfWeek = daysOfWeek()
        binding.legendLayout.root.children.forEachIndexed { index, child ->
            (child as TextView).apply {
                text = daysOfWeek[index].displayText()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColorRes(R.color.text_description)
            }
        }

        configureBinders()
//        val currentMonth = YearMonth.now()

        binding.exFourCalendar.setup(
            startMonth,
            endMonth,
            daysOfWeek.first(),
        )
        binding.exFourCalendar.scrollToMonth(endMonth)

        binding.exFourSaveButton.setOnClickListener click@{
            val (startDate, endDate) = selection
            if (startDate != null && endDate != null) {
                onDateSelectedListener?.onDateSelected(startDate, endDate)
            }
            parentFragmentManager.popBackStack()
        }

        bindSummaryViews()
    }

    private fun bindSummaryViews() {
        binding.exFourStartDateText.apply {
            if (selection.startDate != null) {
                text = headerDateFormatter.format(selection.startDate)
                setTextColorRes(R.color.text_heading)
            } else {
                text = "Start Date"
                setTextColorRes(R.color.text_description)
            }
        }

        binding.exFourEndDateText.apply {
            if (selection.endDate != null) {
                text = headerDateFormatter.format(selection.endDate)
                setTextColorRes(R.color.text_heading)
            } else {
                text = "End Date"
                setTextColorRes(R.color.text_description)
            }
        }

        binding.exFourSaveButton.isEnabled = selection.daysBetween != null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.calendar_menu, menu)
        binding.exFourToolbar.post {
            // Configure menu text to match what is in the Airbnb app.
            binding.exFourToolbar.findViewById<TextView>(R.id.menuItemClear).apply {
                setTextColor(requireContext().getColorCompat(R.color.text_heading))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                isAllCaps = false
            }
        }
        menu.findItem(R.id.menuItemClear).setOnMenuItemClickListener {
            selection = DateSelection()
            binding.exFourCalendar.notifyCalendarChanged()
            bindSummaryViews()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        // close the fragment when the back button is pressed
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun configureBinders() {
        val clipLevelHalf = 5000
        val ctx = requireContext()
        val rangeStartBackground =
            ctx.getDrawableCompat(R.drawable.example_4_continuous_selected_bg_start).also {
                it.level = clipLevelHalf // Used by ClipDrawable
            }
        val rangeEndBackground =
            ctx.getDrawableCompat(R.drawable.example_4_continuous_selected_bg_end).also {
                it.level = clipLevelHalf // Used by ClipDrawable
            }
        val rangeMiddleBackground =
            ctx.getDrawableCompat(R.drawable.example_4_continuous_selected_bg_middle)
        val singleBackground = ctx.getDrawableCompat(R.drawable.example_4_single_selected_bg)
        val todayBackground = ctx.getDrawableCompat(R.drawable.example_4_today_bg)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate &&
                        (day.date.isAfter(firstDate) && day.date.isBefore(today) || day.date == firstDate || day.date == today)
                    ) {
                        selection = getSelection(
                            clickedDate = day.date,
                            dateSelection = selection,
                        )
                        this@CalendarFragment.binding.exFourCalendar.notifyCalendarChanged()
                        bindSummaryViews()
                    }
                }
            }
        }

        binding.exFourCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.binding.exFourDayText
                val roundBgView = container.binding.exFourRoundBackgroundView
                val continuousBgView = container.binding.exFourContinuousBackgroundView

                textView.text = null
                roundBgView.makeInVisible()
                continuousBgView.makeInVisible()

                val (startDate, endDate) = selection

                when (data.position) {
                    DayPosition.MonthDate -> {
                        textView.text = data.date.dayOfMonth.toString()
                        if (data.date.isBefore(firstDate) || data.date.isAfter(today)) {
                            textView.setTextColorRes(R.color.example_4_grey_past)
                        } else {
                            when {
                                startDate == data.date && endDate == null -> {
                                    textView.setTextColorRes(R.color.white)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                data.date == startDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    continuousBgView.applyBackground(rangeStartBackground)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                startDate != null && endDate != null && (data.date > startDate && data.date < endDate) -> {
                                    textView.setTextColorRes(R.color.text_description)
                                    continuousBgView.applyBackground(rangeMiddleBackground)
                                }

                                data.date == endDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    continuousBgView.applyBackground(rangeEndBackground)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                data.date == today -> {
                                    textView.setTextColorRes(R.color.green_main)
                                    roundBgView.applyBackground(todayBackground)
                                }

                                else -> textView.setTextColorRes(R.color.text_heading)
                            }
                        }
                    }
                    // Make the coloured selection background continuous on the
                    // invisible in and out dates across various months.
                    DayPosition.InDate ->
                        if (startDate != null && endDate != null &&
                            isInDateBetweenSelection(data.date, startDate, endDate)
                        ) {
                            continuousBgView.applyBackground(rangeMiddleBackground)
                        }

                    DayPosition.OutDate ->
                        if (startDate != null && endDate != null &&
                            isOutDateBetweenSelection(data.date, startDate, endDate)
                        ) {
                            continuousBgView.applyBackground(rangeMiddleBackground)
                        }
                }
            }

            private fun View.applyBackground(drawable: Drawable) {
                makeVisible()
                background = drawable
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarHeaderBinding.bind(view).exFourHeaderText
        }
        binding.exFourCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.displayText()
                    container.textView.setTextColor(ctx.getColorCompat(R.color.text_heading))
                }
            }
    }

    interface OnDateSelectedListener {
        fun onDateSelected(startDate: LocalDate, endDate: LocalDate)
    }
}