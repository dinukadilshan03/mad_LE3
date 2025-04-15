package com.example.mad_le3

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mad_le3.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.util.*

class Budget : AppCompatActivity() {

    private lateinit var textViewCurrentBudget: TextView
    private lateinit var textViewCurrentSpending: TextView
    private lateinit var textViewRemainingBudget: TextView
    private lateinit var circularProgressBar: CircularProgressBar  // Use CircularProgressBar instead of ProgressBar
    private lateinit var textViewWarning: TextView
    private lateinit var buttonSaveBudget: Button
    private lateinit var editTextMonthlyBudget: EditText

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val transactionKey = "transactions_list"
    private val budgetKey = "monthly_budget"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.budgetmain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Initialize views
        textViewCurrentBudget = findViewById(R.id.textViewCurrentBudget)
        textViewCurrentSpending = findViewById(R.id.textViewCurrentSpending)
        textViewRemainingBudget = findViewById(R.id.textViewRemainingBudget)
        circularProgressBar = findViewById(R.id.budgetProgressBar)  // Use CircularProgressBar
        textViewWarning = findViewById(R.id.textViewWarning)
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget)
        editTextMonthlyBudget = findViewById(R.id.editTextMonthlyBudget)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)

        // Load the current budget from SharedPreferences
        val savedBudget = sharedPreferences.getFloat(budgetKey, 0f)
        textViewCurrentBudget.text = "Current Budget: $savedBudget"

        // Calculate current spending
        val totalExpenses = calculateTotalExpenses()
        textViewCurrentSpending.text = "Current Spending: $totalExpenses"

        // Calculate the remaining budget
        val remainingBudget = savedBudget - totalExpenses
        textViewRemainingBudget.text = "Remaining Budget: $remainingBudget"

        // Update CircularProgressBar after calculations
        val progress = if (savedBudget > 0) {
            ((totalExpenses / savedBudget) * 100).toFloat()
        } else {
            0f
        }

        circularProgressBar.apply {
            // Set Progress (with the new blue color for progress)
            progressBarColor = Color.parseColor("#2869FE")  // Blue color for progress

            // Optionally, you can set a gradient for the progress
            progressBarColorStart = Color.parseColor("#2869FE")  // Start color (blue)
            progressBarColorEnd = Color.parseColor("#1E4C9A")    // End color (darker blue)
            progressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM

            // Set background ProgressBar Color (lighter shade for the background)
            backgroundProgressBarColor = Color.parseColor("#A0C8FF")  // Lighter blue color for background
            // Optionally, you can set a gradient for the background (lighter blue gradient)
            backgroundProgressBarColorStart = Color.parseColor("#A0C8FF")  // Lighter blue
            backgroundProgressBarColorEnd = Color.parseColor("#A0C8FF")    // Lighter blue for background
            backgroundProgressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM

            // Set Width for both progress and background progress bars
            progressBarWidth = 30f  // Thicker progress bar
            backgroundProgressBarWidth = 50f  // Thicker background bar

            // Other configurations
            roundBorder = false  // Set to true for rounded corners, false for sharp corners
            startAngle = 180f  // Start angle for the circular progress bar
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT  // Fill direction (left to right)
        }

        // Log the progress for debugging
        Log.d("BudgetActivity", "Progress: $progress")

        calculateRemainingBudget()

        // Save the new budget when the user clicks the save button
        buttonSaveBudget.setOnClickListener {
            val newBudget = editTextMonthlyBudget.text.toString().toFloatOrNull()
            if (newBudget != null) {
                saveBudget(newBudget)
                Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid budget", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to save the budget to SharedPreferences
    fun saveBudget(budget: Float) {
        // Save the new budget in SharedPreferences
        sharedPreferences.edit().putFloat(budgetKey, budget).apply()
        // Update the UI
        textViewCurrentBudget.text = "Current Budget: $budget"
        // Recalculate and update the remaining budget and progress bar
        calculateRemainingBudget()
    }

    // Function to calculate total expenses for the current month
    fun calculateTotalExpenses(): Float {
        val allTransactions = loadTransactions()
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // Get current month (1-12)
        var totalExpenses = 0f

        // Filter transactions for the current month and only expenses
        for (transaction in allTransactions) {
            val transactionDate = transaction.date.split("-")
            val transactionMonth = transactionDate[1].toInt() // Extract month from transaction date

            if (transaction.type == "Expense" && transactionMonth == currentMonth) {
                totalExpenses += transaction.amount.toFloat()
            }
        }
        return totalExpenses
    }

    // Function to load transactions from SharedPreferences
    fun loadTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(transactionKey, null)
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: listOf()
    }

    // Function to calculate and update remaining budget
    fun calculateRemainingBudget() {
        // Load saved budget from SharedPreferences
        val savedBudget = sharedPreferences.getFloat(budgetKey, 0f)

        // Calculate the total expenses for the current month
        val totalExpenses = calculateTotalExpenses()

        // Log the values for debugging
        Log.d("BudgetActivity", "Saved Budget: $savedBudget")
        Log.d("BudgetActivity", "Total Expenses: $totalExpenses")

        // Calculate the remaining budget
        val remainingBudget = savedBudget - totalExpenses

        // Display the remaining budget in the TextView
        textViewRemainingBudget.text = "Remaining Budget: $remainingBudget"

        // Calculate the progress
        val progress = if (savedBudget > 0) {
            ((totalExpenses / savedBudget) * 100).toFloat() // Percentage of budget spent
        } else {
            0f
        }

        // Log the progress value
        Log.d("BudgetActivity", "Progress: $progress")

        // Update the CircularProgressBar with the calculated progress
        circularProgressBar.setProgressWithAnimation(progress, 1000)  // Use setProgressWithAnimation for smooth animation

        // Show warning if the budget is exceeded
        if (remainingBudget < 0) {
            textViewWarning.text = "Warning: You have exceeded your budget!"
        } else {
            textViewWarning.text = ""
        }
    }
}
