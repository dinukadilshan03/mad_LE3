package com.example.mad_le3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_le3.model.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Dashboard : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var tvWelcome: TextView
    private lateinit var tvCurrentBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpenses: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var ivSettings: ImageView // Added for settings icon
    private val gson = Gson()
    private val transactionKey = "transactions_list"

    companion object {
        const val EDIT_TRANSACTION_REQUEST_CODE_DASHBOARD = 456 // Different request code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        pieChart = findViewById(R.id.pieChart)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpenses = findViewById(R.id.tvExpenses)
        rvTransactions = findViewById(R.id.rvTransactions)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        ivSettings = findViewById(R.id.ivSettings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)

        // Load transactions and calculate values
        val transactions = loadTransactions()
        val totalIncome = calculateTotalIncome(transactions)
        val totalExpenses = calculateTotalExpenses(transactions)
        val balance = totalIncome - totalExpenses

        // Set the calculated values to TextViews
        tvIncome.text = String.format("Rs. %.2f", totalIncome)
        tvExpenses.text = String.format("Rs. %.2f", totalExpenses)
        tvCurrentBalance.text = String.format("Rs. %.2f", balance)

        // Set up RecyclerView for transactions
        setupRecyclerView()

        setupPieChart(transactions)

        // Set up BottomNavigationView
        bottomNavigationView.selectedItemId = R.id.nav_dashboard
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTransaction::class.java))
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, Budget::class.java))
                    true
                }
                R.id.nav_backup -> {
                    startActivity(Intent(this, Transactions::class.java))
                    true
                }
                else -> false
            }
        }

        // Apply padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Set click listener for the settings icon
        ivSettings.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }
    }

    // Set up RecyclerView with TransactionAdapter
    private fun setupRecyclerView() {
        val transactions = loadTransactions()
        val sortedTransactions = transactions.sortedByDescending { it.getDateAsDate() }

        rvTransactions.layoutManager = LinearLayoutManager(this)

        transactionAdapter = TransactionAdapter(sortedTransactions) { transaction ->
            val intent = Intent(this, EditTransaction::class.java)
            intent.putExtra("transaction", transaction)
            startActivityForResult(intent, EDIT_TRANSACTION_REQUEST_CODE_DASHBOARD) // Start for result
        }

        rvTransactions.adapter = transactionAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_TRANSACTION_REQUEST_CODE_DASHBOARD) {
            if (resultCode == Activity.RESULT_OK + 1) { // EditTransaction.TRANSACTION_UPDATED_RESULT_CODE
                val updatedTransaction = data?.getParcelableExtra<Transaction>(EditTransaction.UPDATED_TRANSACTION_EXTRA)
                updatedTransaction?.let {
                    val transactions = loadTransactions().toMutableList()
                    val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
                    if (index != -1) {
                        transactions[index] = it
                        saveTransactions(transactions) // Save the updated list
                        setupRecyclerView() // Reload and refresh the RecyclerView
                        setupPieChart(transactions) // Refresh the pie chart
                    }
                }
            } else if (resultCode == Activity.RESULT_OK + 2) { // Handling for delete
                val transactions = loadTransactions().toMutableList()
                val deletedTransactionIdString = data?.getStringExtra("deleted_transaction_id")
                deletedTransactionIdString?.toLongOrNull()?.let { deletedTransactionIdLong ->
                    transactions.removeAll { it.id == deletedTransactionIdLong }
                    saveTransactions(transactions) // Save the updated list
                    setupRecyclerView() // Reload and refresh the RecyclerView
                    setupPieChart(transactions) // Refresh the pie chart
                }
            }
        }
    }

    // Set up PieChart with spending breakdown data
    private fun setupPieChart(transactions: List<Transaction>) {
        val categoryExpenses = calculateCategoryExpenses(transactions)
        val pieEntries = mutableListOf<PieEntry>()
        categoryExpenses.forEach { (category, amount) ->
            pieEntries.add(PieEntry(amount, category))
        }
        val pieDataSet = PieDataSet(pieEntries, "Spending Breakdown")
        pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    // Calculate total expenses per category
    private fun calculateCategoryExpenses(transactions: List<Transaction>): Map<String, Float> {
        val categoryExpenses = mutableMapOf<String, Float>()
        for (transaction in transactions) {
            if (transaction.type == "Expense") {
                categoryExpenses[transaction.category] = categoryExpenses.getOrDefault(transaction.category, 0f) + transaction.amount.toFloat()
            }
        }
        return categoryExpenses
    }

    // Function to load transactions from SharedPreferences
    private fun loadTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(transactionKey, null)
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: listOf()
    }

    // Function to save transactions to SharedPreferences
    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(transactionKey, json).apply()
    }

    // Calculate total income
    private fun calculateTotalIncome(transactions: List<Transaction>): Double {
        return transactions.filter { it.type == "Income" }.sumOf { it.amount }
    }

    // Calculate total expenses
    private fun calculateTotalExpenses(transactions: List<Transaction>): Double {
        return transactions.filter { it.type == "Expense" }.sumOf { it.amount }
    }
}