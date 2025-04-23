package com.example.mad_le3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_le3.model.CategorySpending

class CategorySpendingAdapter(private val categorySpendingList: List<CategorySpending>) :
    RecyclerView.Adapter<CategorySpendingAdapter.CategorySpendingViewHolder>() {

    class CategorySpendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.textViewCategoryName)
        val categoryAmountTextView: TextView = itemView.findViewById(R.id.textViewCategoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySpendingViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_spending, parent, false)
        return CategorySpendingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategorySpendingViewHolder, position: Int) {
        val currentItem = categorySpendingList[position]
        holder.categoryNameTextView.text = currentItem.category
        holder.categoryAmountTextView.text = String.format("%.2f", currentItem.totalSpent) // Format to 2 decimal places
    }

    override fun getItemCount() = categorySpendingList.size
}