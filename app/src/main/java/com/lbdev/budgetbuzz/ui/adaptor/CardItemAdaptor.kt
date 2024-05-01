package com.lbdev.budgetbuzz.ui.adaptor

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
import com.lbdev.budgetbuzz.data.model.Category

class CardItemAdaptor(
    private val items: List<Category>, private val listener: OnItemClickListener
) : RecyclerView.Adapter<CardItemAdaptor.CardItemViewHolder>() {

    private var selectedPosition = -1

    class CardItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.categoryIconImageView)
        val textView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val card: MaterialCardView = itemView.findViewById(R.id.categoryCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CardItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardItemViewHolder, position: Int) {
        val item = items[position]

        holder.imageView.load(item.icon) {
            decoderFactory { result, options, _ -> SvgDecoder(result.source, options) }
        }
        holder.textView.text = item.name
        holder.textView.setTextColor(holder.itemView.context.resources.getColor(R.color.white,null))
        holder.card.isChecked = selectedPosition == position

        holder.imageView.setColorFilter(
            if (selectedPosition == position) holder.itemView.context.resources.getColor(
                R.color.activity_background, null
            )
            else holder.itemView.context.resources.getColor(R.color.text_heading, null)
        )

        holder.textView.setTextColor(
            if (selectedPosition == position) holder.itemView.context.resources.getColor(
                R.color.white, null
            )
            else holder.itemView.context.resources.getColor(R.color.text_heading, null)
        )

        holder.card.setBackgroundDrawable(
            if (selectedPosition == position) {
//                android.graphics.Color.parseColor(item.startColor)
                val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    intArrayOf(Color.parseColor(item.startColor), Color.parseColor(item.endColor))
                )
                gradientDrawable
            } else {
//                holder.itemView.context.resources.getColor(R.color.activity_background)
                val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BL_TR, intArrayOf(
                        holder.itemView.context.resources.getColor(R.color.activity_background,null),
                        holder.itemView.context.resources.getColor(R.color.activity_background,null)
                    )
                )
                gradientDrawable
            }
        )
        holder.itemView.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = position
            notifyItemChanged(selectedPosition)
            listener.onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    interface OnItemClickListener {
        fun onItemClick(item: Category)
    }
}
