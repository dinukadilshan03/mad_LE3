package com.example.mad_le3

import android.app.Activity
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
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button
    private lateinit var transaction: Transaction
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val transactionKey = "transactions_list"

    companion object {
        const val TRANSACTION_UPDATED_RESULT_CODE = Activity.RESULT_OK + 1
        const val UPDATED_TRANSACTION_EXTRA = "updated_transaction"
        const val TRANSACTION_DELETED_RESULT_CODE = Activity.RESULT_OK + 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction) // Use the edit_transaction layout

        // Initialize views using the IDs from edit_transaction.xml
        titleEditText = findViewById(R.id.editTextEditTransactionTitle)
        amountEditText = findViewById(R.id.editTextEditAmount)
        categorySpinner = findViewById(R.id.spinnerEditCategory)
        incomeRadioButton = findViewById(R.id.radioButtonEditIncome)
        expenseRadioButton = findViewById(R.id.radioButtonEditExpense)
        updateButton = findViewById(R.id.buttonUpdateTransaction)
        deleteButton = findViewById(R.id.buttonDeleteTransaction)
        cancelButton = findViewById(R.id.buttonCancelEditTransaction)

        // Get the transaction passed from the previous activity
        transaction = intent.getParcelableExtra<Transaction>("transaction")!!

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

        // Set up Update button click listener
        updateButton.setOnClickListener {
            updateTransaction(transaction)
        }

        // Set up Delete button click listener
        deleteButton.setOnClickListener {
            deleteTransaction(transaction)
        }

        // Set up Cancel button click listener
        cancelButton.setOnClickListener {
            finish() // Go back to the Transactions activity
        }


    }

    private fun updateTransaction(originalTransaction: Transaction) {
        // Get updated data from UI
        val updatedTitle = titleEditText.text.toString()
        val updatedAmount = amountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val updatedCategory = categorySpinner.selectedItem.toString()
        val updatedType = if (incomeRadioButton.isChecked) "Income" else "Expense"

        // Create the updated transaction
        val updatedTransaction = Transaction(
            id = originalTransaction.id,
            title = updatedTitle,
            amount = updatedAmount,
            category = updatedCategory,
            type = updatedType,
            date = originalTransaction.date  // Date can be updated if needed (you might want to add a date picker)
        )

        // Save updated transaction to SharedPreferences
        saveTransaction(updatedTransaction)

        // Create an Intent to send back the updated transaction
        val resultIntent = Intent()
        resultIntent.putExtra(UPDATED_TRANSACTION_EXTRA, updatedTransaction)
        setResult(TRANSACTION_UPDATED_RESULT_CODE, resultIntent)
        finish()  // Go back to the Transactions activity
    }

    private fun deleteTransaction(transactionToDelete: Transaction) {
        val transactions = loadTransactions()
        val updatedTransactions = transactions.filter { it.id != transactionToDelete.id }
        saveTransactions(updatedTransactions)

        val resultIntent = Intent()
        resultIntent.putExtra("deleted_transaction_id", transactionToDelete.id.toString()) // Pass the ID as a String
        setResult(TRANSACTION_DELETED_RESULT_CODE, resultIntent)
        finish()
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

    private fun saveTransaction(transactionToSave: Transaction) {
        val transactions = loadTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transactionToSave.id }

        // If the transaction is found, update it
        if (index != -1) {
            transactions[index] = transactionToSave
        }
        saveTransactions(transactions)
    }

    private fun getCategoryPosition(category: String): Int {
        // Return the index of the category in the spinner
        val categories = resources.getStringArray(R.array.transaction_categories_array)
        return categories.indexOf(category)
    }
}