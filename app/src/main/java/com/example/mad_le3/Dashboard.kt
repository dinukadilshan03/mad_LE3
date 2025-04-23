package com.example.mad_le3

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        pieChart = findViewById(R.id.pieChart)  // Reference the PieChart
        tvWelcome = findViewById(R.id.tvWelcome)
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpenses = findViewById(R.id.tvExpenses)
        rvTransactions = findViewById(R.id.rvTransactions)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        ivSettings = findViewById(R.id.ivSettings) // Initialize the settings icon ImageView



        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)



        // Load transactions from SharedPreferences and calculate values
        val transactions = loadTransactions()
        val totalIncome = calculateTotalIncome(transactions)
        val totalExpenses = calculateTotalExpenses(transactions)
        val balance = totalIncome - totalExpenses

        // Set the calculated values to TextViews
        tvIncome.text = String.format("Rs. %.2f", totalIncome)
        tvExpenses.text = String.format("Rs. %.2f", totalExpenses)
        tvCurrentBalance.text = String.format("Rs. %.2f", balance)

        // Set up RecyclerView for transactions (get transactions from SharedPreferences)
        setupRecyclerView()

        setupPieChart(transactions)


        // Get reference to BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.nav_dashboard

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

        // Apply padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set click listener for the settings icon
        ivSettings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }

    // Set up RecyclerView with TransactionAdapter
    private fun setupRecyclerView() {
        val transactions = loadTransactions()  // Load transactions from SharedPreferences

        // Sort transactions by date (newest first)
        val sortedTransactions = transactions.sortedByDescending { it.getDateAsDate() }

        rvTransactions.layoutManager = LinearLayoutManager(this)  // Use vertical layout for RecyclerView

        // Pass the onItemClickListener to the adapter
        transactionAdapter = TransactionAdapter(sortedTransactions) { transaction ->
            // When an item is clicked, navigate to EditTransaction activity
            val intent = Intent(this, EditTransaction::class.java)
            intent.putExtra("transaction", transaction)  // Pass the selected transaction data
            startActivity(intent)
        }

        rvTransactions.adapter = transactionAdapter // Set the adapter to the RecyclerView
    }


    // Set up PieChart with spending breakdown data
    private fun setupPieChart(transactions: List<Transaction>) {
        // Calculate total expenses per category
        val categoryExpenses = calculateCategoryExpenses(transactions)

        val pieEntries = mutableListOf<PieEntry>()
        categoryExpenses.forEach { (category, amount) ->
            pieEntries.add(PieEntry(amount, category))
        }

        val pieDataSet = PieDataSet(pieEntries, "Spending Breakdown")
        pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()  // Apply colorful colors

        val pieData = PieData(pieDataSet)
        pieChart.data = pieData

        // Apply animation to the pie chart
        pieChart.animateY(1000)  // Animation duration of 1 second

        // Refresh the chart
        pieChart.invalidate()
    }

    // Calculate total expenses per category
    private fun calculateCategoryExpenses(transactions: List<Transaction>): Map<String, Float> {
        val categoryExpenses = mutableMapOf<String, Float>()

        // Sum up expenses per category
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
        return gson.fromJson(json, type) ?: listOf()  // Return an empty list if no transactions exist
    }

    // Calculate total income by summing up the "Income" type transactions
    private fun calculateTotalIncome(transactions: List<Transaction>): Double {
        return transactions.filter { it.type == "Income" }
            .sumOf { it.amount }
    }

    // Calculate total expenses by summing up the "Expense" type transactions
    private fun calculateTotalExpenses(transactions: List<Transaction>): Double {
        return transactions.filter { it.type == "Expense" }
            .sumOf { it.amount }
    }
}
