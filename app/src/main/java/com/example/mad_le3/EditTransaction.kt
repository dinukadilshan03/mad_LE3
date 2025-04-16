package com.example.mad_le3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mad_le3.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EditTransaction : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var incomeRadioButton: RadioButton
    private lateinit var expenseRadioButton: RadioButton
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var transaction: Transaction
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val transactionKey = "transactions_list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction) // Reuse the AddTransaction layout

        // Initialize views
        titleEditText = findViewById(R.id.editTextTransactionTitle)
        amountEditText = findViewById(R.id.editTextAmount)
        categorySpinner = findViewById(R.id.spinnerCategory)
        incomeRadioButton = findViewById(R.id.radioButtonIncome)
        expenseRadioButton = findViewById(R.id.radioButtonExpense)
        saveButton = findViewById(R.id.buttonSaveTransaction)
        deleteButton = findViewById(R.id.buttonCancelTransaction)

        // Get the transaction passed from the previous activity
        transaction = intent.getParcelableExtra("transaction")!!

        // Pre-fill the fields with the transaction data
        titleEditText.setText(transaction.title)
        amountEditText.setText(transaction.amount.toString())
        categorySpinner.setSelection(getCategoryPosition(transaction.category))
        if (transaction.type == "Income") {
            incomeRadioButton.isChecked = true
        } else {
            expenseRadioButton.isChecked = true
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)

        // Set up Save button click listener
        saveButton.setOnClickListener {
            updateTransaction(transaction)
        }

        // Set up Delete button click listener
        deleteButton.setOnClickListener {
            deleteTransaction(transaction)
        }
    }

    private fun updateTransaction(transaction: Transaction) {
        // Get updated data from UI
        val updatedTitle = titleEditText.text.toString()
        val updatedAmount = amountEditText.text.toString().toDouble()
        val updatedCategory = categorySpinner.selectedItem.toString()
        val updatedType = if (incomeRadioButton.isChecked) "Income" else "Expense"

        // Create the updated transaction
        val updatedTransaction = Transaction(
            id = transaction.id,
            title = updatedTitle,
            amount = updatedAmount,
            category = updatedCategory,
            type = updatedType,
            date = transaction.date  // Date can be updated if needed
        )

        // Save updated transaction to SharedPreferences
        saveTransaction(updatedTransaction)

        Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        finish()  // Go back to the Transactions activity
    }

    private fun deleteTransaction(transaction: Transaction) {
        // Get the current list of transactions
        val transactions = loadTransactions()

        // Remove the deleted transaction
        val updatedTransactions = transactions.filter { it.id != transaction.id }

        // Save the updated list to SharedPreferences
        saveTransactions(updatedTransactions)

        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
        finish()  // Go back to the Transactions activity
    }

    private fun loadTransactions(): MutableList<Transaction> {
        val json = sharedPreferences.getString(transactionKey, null)
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(transactionKey, json).apply()
    }

    private fun saveTransaction(transaction: Transaction) {
        val transactions = loadTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }

        // If the transaction is found, update it, else add the new transaction
        if (index != -1) {
            transactions[index] = transaction
        } else {
            transactions.add(transaction)
        }

        saveTransactions(transactions)
    }

    private fun getCategoryPosition(category: String): Int {
        // Return the index of the category in the spinner
        val categories = resources.getStringArray(R.array.transaction_categories_array)
        return categories.indexOf(category)
    }
}
