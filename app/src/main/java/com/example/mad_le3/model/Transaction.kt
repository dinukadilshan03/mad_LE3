package com.example.mad_le3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val amount: Double,
    val category: String,
    val type: String, // "Income" or "Expense"
    val date: String // Or a Date object
) : Parcelable{

    fun getDateAsDate(): Date {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.parse(date) ?: Date() // Return current date if parsing fails
    }
}