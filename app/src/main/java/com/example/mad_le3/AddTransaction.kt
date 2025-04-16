package com.example.mad_le3

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mad_le3.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.Locale

class AddTransaction : AppCompatActivity() {

    private lateinit var dateTextView: TextView
    private lateinit var datePickerButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var incomeRadioButton: RadioButton
    private lateinit var expenseRadioButton: RadioButton
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val transactionKey = "transactions_list"
    private val calendar = Calendar.getInstance(Locale.getDefault())
    private var selectedYear = calendar.get(Calendar.YEAR)
    private var selectedMonth = calendar.get(Calendar.MONTH)
    private var selectedDay = calendar.get(Calendar.DAY_OF_MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        dateTextView = findViewById(R.id.selectedDateTextView)
        datePickerButton = findViewById(R.id.buttonDatePicker)
        titleEditText = findViewById(R.id.editTextTransactionTitle)
        amountEditText = findViewById(R.id.editTextAmount)
        categorySpinner = findViewById(R.id.spinnerCategory)
        incomeRadioButton = findViewById(R.id.radioButtonIncome)
        expenseRadioButton = findViewById(R.id.radioButtonExpense)
        saveButton = findViewById(R.id.buttonSaveTransaction)
        cancelButton = findViewById(R.id.buttonCancelTransaction)
        sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)

        updateDateTextView()

        datePickerButton.setOnClickListener {
            showDatePickerDialog()
        }

        saveButton.setOnClickListener {
            saveTransaction()
        }

        cancelButton.setOnClickListener {
            finish() // Go back to the previous activity
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addTransactionLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

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

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                selectedYear = year
                selectedMonth = monthOfYear
                selectedDay = dayOfMonth
                updateDateTextView()
            },
            selectedYear,
            selectedMonth,
            selectedDay
        )
        datePickerDialog.show()
    }

    private fun updateDateTextView() {
        val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
        dateTextView.text = formattedDate
    }

    private fun saveTransaction() {
        val title = titleEditText.text.toString().trim()
        val amountStr = amountEditText.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()
        val type = when {
            incomeRadioButton.isChecked -> "Income"
            expenseRadioButton.isChecked -> "Expense"
            else -> ""
        }
        val date = dateTextView.text.toString()

        if (title.isNotEmpty() && amountStr.isNotEmpty() && type.isNotEmpty() && category != getString(R.string.pick_a_category) && date.isNotEmpty()) {
            try {
                val amount = amountStr.toDouble()
                val newTransaction = Transaction(title = title, amount = amount, category = category, type = type, date = date)
                saveNewTransaction(newTransaction)
                Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
                // Explicitly start the Transactions Activity
                val intent = android.content.Intent(this, Transactions::class.java)
                startActivity(intent)
                finish() // Optional: Close the AddTransaction Activity after navigating
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTransactions(): MutableList<Transaction> {
        val json = sharedPreferences.getString(transactionKey, null)
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        Log.d("AddTransaction", "Saving transactions to SharedPreferences: $json")
        sharedPreferences.edit().putString(transactionKey, json).apply()
    }

    private fun saveNewTransaction(newTransaction: Transaction) {
        val existingTransactions = loadTransactions()
        existingTransactions.add(newTransaction)
        saveTransactions(existingTransactions)
    }
}