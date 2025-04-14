package com.example.mad_le3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val amount: Double,
    val category: String,
    val type: String, // "Income" or "Expense"
    val date: String // Or a Date object
) : Parcelable