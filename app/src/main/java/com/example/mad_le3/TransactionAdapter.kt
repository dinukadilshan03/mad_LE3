package com.example.mad_le3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_le3.model.Transaction

class TransactionAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.transactionTitleTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.transactionAmountTextView)
        val categoryTextView: TextView = itemView.findViewById(R.id.transactionCategoryTextView)
        val typeTextView: TextView = itemView.findViewById(R.id.transactionTypeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction = transactions[position]
        holder.titleTextView.text = currentTransaction.title
        holder.amountTextView.text = String.format("%.2f $", currentTransaction.amount)
        if (currentTransaction.type == "Expense") {
            holder.amountTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.holo_red_dark, null))
        } else {
            holder.amountTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.holo_green_dark, null))
        }
        holder.categoryTextView.text = currentTransaction.category
        holder.typeTextView.text = currentTransaction.type
    }

    override fun getItemCount() = transactions.size
}