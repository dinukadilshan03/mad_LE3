package com.example.mad_le3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_le3.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.mad_le3.model.CategorySpending


class CategorySpending : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val transactionKey = "transactions_list"
    private lateinit var categorySpendingRecyclerView: RecyclerView
    private lateinit var categorySpendingAdapter: CategorySpendingAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_spending)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)
        categorySpendingRecyclerView = findViewById(R.id.categorySpendingRecyclerView)
        categorySpendingRecyclerView.layoutManager = LinearLayoutManager(this)

        val categorySpendingData = calculateCategorySpending()
        categorySpendingAdapter = CategorySpendingAdapter(categorySpendingData)
        categorySpendingRecyclerView.adapter = categorySpendingAdapter


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.nav_add

        // Use the new method setOnItemSelectedListener to handle item clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Navigate to HomeActivity
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_add -> {
                    // Navigate to SearchActivity
                    val intent = Intent(this, AddTransaction::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_budget -> {
                    // Navigate to ProfileActivity
                    val intent = Intent(this, Budget::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_backup -> {
                    // Navigate to ProfileActivity
                    val intent = Intent(this, Transactions::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    private fun loadTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(transactionKey, null)
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun calculateCategorySpending(): List<CategorySpending> {
        val transactions = loadTransactions()
        val categorySpendingMap = mutableMapOf<String, Double>()

        for (transaction in transactions) {
            if (transaction.type == "Expense") {
                val currentSpending = categorySpendingMap.getOrDefault(transaction.category, 0.0)
                categorySpendingMap[transaction.category] = currentSpending + transaction.amount
            }
        }

        val categorySpendingList = mutableListOf<CategorySpending>()
        for ((category, totalSpent) in categorySpendingMap) {
            categorySpendingList.add(CategorySpending(category, totalSpent))
        }

        // Optionally sort the list by spending amount or category name
        categorySpendingList.sortByDescending { it.totalSpent } // Example: Sort by highest spending

        return categorySpendingList
    }
}