package com.example.mad_le3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_le3.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Transactions : AppCompatActivity() {

    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var viewSpendingButton: Button
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var filterTypeSpinner: Spinner
    private lateinit var filterCategorySpinner: Spinner
    private var allTransactions: MutableList<Transaction> = mutableListOf()
    private var filteredTransactions: MutableList<Transaction> = mutableListOf()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val transactionKey = "transactions_list"

    companion object {
        const val EDIT_TRANSACTION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions) // Make sure this matches your XML

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transactionsLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        viewSpendingButton = findViewById(R.id.buttonViewSpending)

        viewSpendingButton.setOnClickListener {
            val intent = Intent(this, CategorySpending::class.java)
            startActivity(intent)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.nav_backup

        // Use the new method setOnItemSelectedListener to handle item clicks
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_add -> {
                    val intent = Intent(this, AddTransaction::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_budget -> {
                    val intent = Intent(this, Budget::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_backup -> {
                    val intent = Intent(this, Transactions::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        filterTypeSpinner = findViewById(R.id.filterTypeSpinner)
        filterCategorySpinner = findViewById(R.id.filterCategorySpinner)

        sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)

        // Load transactions from Shared Preferences
        loadTransactions()
        filteredTransactions.addAll(allTransactions)

        // Set up RecyclerView
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Pass the listener directly when initializing the adapter
        transactionAdapter = TransactionAdapter(filteredTransactions) { transaction ->
            // When an item is clicked, navigate to EditTransaction activity for result
            val intent = Intent(this, EditTransaction::class.java)
            intent.putExtra("transaction", transaction)  // Pass the selected transaction data
            startActivityForResult(intent, EDIT_TRANSACTION_REQUEST_CODE) // Use startActivityForResult
        }

        transactionsRecyclerView.adapter = transactionAdapter // Set the adapter to the RecyclerView

        // Populate filter spinners
        setupTypeFilterSpinner()
        setupCategoryFilterSpinner()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_TRANSACTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK + 1) { // EditTransaction.TRANSACTION_UPDATED_RESULT_CODE
                val updatedTransaction = data?.getParcelableExtra<Transaction>(EditTransaction.UPDATED_TRANSACTION_EXTRA)
                updatedTransaction?.let {
                    val index = allTransactions.indexOfFirst { it.id == updatedTransaction.id }
                    if (index != -1) {
                        allTransactions[index] = it
                        filterTransactions(filterTypeSpinner.selectedItem?.toString(), filterCategorySpinner.selectedItem?.toString())
                        saveTransactions() // Save the updated list
                    }
                }
            } else if (resultCode == Activity.RESULT_OK + 2) { // EditTransaction.TRANSACTION_DELETED_RESULT_CODE
                val deletedTransactionIdString = data?.getStringExtra("deleted_transaction_id")
                deletedTransactionIdString?.toLongOrNull()?.let { deletedTransactionIdLong ->
                    allTransactions.removeAll { it.id == deletedTransactionIdLong }
                    filterTransactions(
                        filterTypeSpinner.selectedItem?.toString(),
                        filterCategorySpinner.selectedItem?.toString()
                    )
                    saveTransactions() // Save the updated list
                }
                }
                else {
                    // If you didn't pass the ID, simply reload
                    loadTransactions()
                    filterTransactions(filterTypeSpinner.selectedItem?.toString(), filterCategorySpinner.selectedItem?.toString())
                }

        }
    }

    private fun loadTransactions() {
        val json = sharedPreferences.getString(transactionKey, null)
        json?.let {
            val type = object : TypeToken<List<Transaction>>() {}.type
            allTransactions = gson.fromJson(it, type) ?: mutableListOf()
        }
        Log.d("Transactions", "loadTransactions() called")
        Log.d("Transactions", "Loaded JSON: $json")
        Log.d("Transactions", "allTransactions size after load: ${allTransactions.size}")
    }

    private fun saveTransactions() {
        val json = gson.toJson(allTransactions)
        sharedPreferences.edit().putString(transactionKey, json).apply()
    }

    private fun setupTypeFilterSpinner() {
        val typeOptions = listOf("All", "Income", "Expense")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeOptions)
        filterTypeSpinner.adapter = adapter

        filterTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = typeOptions[position]
                filterTransactions(selectedType, filterCategorySpinner.selectedItem?.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupCategoryFilterSpinner() {
        val categoryOptions = mutableListOf("All")
        allTransactions.forEach { if (!categoryOptions.contains(it.category)) categoryOptions.add(it.category) }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)
        filterCategorySpinner.adapter = adapter

        filterCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categoryOptions[position]
                filterTransactions(filterTypeSpinner.selectedItem?.toString(), selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun filterTransactions(typeFilter: String?, categoryFilter: String?) {
        filteredTransactions.clear()
        allTransactions.forEach { transaction ->
            val typeMatch = typeFilter == "All" || transaction.type == typeFilter
            val categoryMatch = categoryFilter == "All" || transaction.category == categoryFilter
            if (typeMatch && categoryMatch) {
                filteredTransactions.add(transaction)
            }
        }
        transactionAdapter.notifyDataSetChanged()
    }
}