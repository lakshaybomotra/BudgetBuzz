package com.lbdev.budgetbuzz.ui.adaptor

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import com.google.android.material.card.MaterialCardView
import com.lbdev.budgetbuzz.R
import com.lbdev.budgetbuzz.data.model.Transaction
import java.util.Calendar

class TransactionsAdaptor(
    private val items: List<Transaction>
) : RecyclerView.Adapter<TransactionsAdaptor.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryCard: MaterialCardView = itemView.findViewById(R.id.cvTransactionType)
        val categoryImageView: ImageView = itemView.findViewById(R.id.ivTransactionType)
        val categoryTextView: TextView = itemView.findViewById(R.id.tvTransactionCategory)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvTransactionDescription)
        val dateTextView: TextView = itemView.findViewById(R.id.tvTransactionDate)
        val amountTextView: TextView = itemView.findViewById(R.id.tvTransactionAmount)
        val signTextView: TextView = itemView.findViewById(R.id.tvTransactionSign)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = items[position]

        holder.categoryTextView.text = item.category.name
        holder.descriptionTextView.text = item.note
        holder.amountTextView.text = item.amount
        holder.signTextView.text = if (item.type == "Expense") "-" else "+"

        holder.categoryImageView.load(item.category.icon) {
            decoderFactory { result, options, _ -> SvgDecoder(result.source, options) }
        }
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(
                Color.parseColor(item.category.startColor),
                Color.parseColor(item.category.endColor)
            )
        )
        holder.categoryCard.setBackgroundDrawable(gradientDrawable)

        val date = item.date.toDate()
        holder.dateTextView.text = run {
            val cal = Calendar.getInstance()
            cal.time = date
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            "$day/${month + 1}/$year"
        }

        setAnimation(holder.itemView, position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                on_attach = false
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        super.onAttachedToRecyclerView(recyclerView)
    }

    private var lastPosition = -1
    private var on_attach = true
    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
           animateBottomUp(view, if (on_attach) position else -1)
            lastPosition = position
        }
    }

    private fun animateBottomUp(view: View, pos: Int) {
        var position = pos
        val notFirstItem = position == -1
        position += 1
        view.translationY = (if (notFirstItem) 800 else 500).toFloat()
        view.alpha = 0f
        val animatorSet = AnimatorSet()
        val animatorTranslateY = ObjectAnimator.ofFloat(
            view,
            "translationY",
            (if (notFirstItem) 800 else 500).toFloat(),
            0f
        )
        val animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 1f)
        animatorTranslateY.startDelay = (if (notFirstItem) 0 else position * 150).toLong()
        animatorTranslateY.setDuration(((if (notFirstItem) 3 else 1) * 150).toLong())
        animatorSet.playTogether(animatorTranslateY, animatorAlpha)
        animatorSet.start()
    }

    override fun getItemCount() = items.size
}