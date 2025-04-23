package com.example.mad_le3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mad_le3.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.util.*
import android.Manifest

class Budget : AppCompatActivity() {

    private lateinit var textViewCurrentBudget: TextView
    private lateinit var textViewCurrentSpending: TextView
    private lateinit var textViewRemainingBudget: TextView
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var textViewWarning: TextView
    private lateinit var buttonSaveBudget: Button
    private lateinit var editTextMonthlyBudget: EditText

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val transactionKey = "transactions_list"
    private val budgetKey = "monthly_budget"
    private val BUDGET_EXCEEDED_NOTIFICATION_ID = 1001
    private val BUDGET_80_PERCENT_NOTIFICATION_ID = 1002
    private val BUDGET_NOTIFICATION_CHANNEL_ID = "budget_alerts"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.budgetmain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.selectedItemId = R.id.nav_budget

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, Dashboard::class.java))
                    true
                }
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

        // Initialize views
        textViewCurrentBudget = findViewById(R.id.textViewCurrentBudget)
        textViewCurrentSpending = findViewById(R.id.textViewCurrentSpending)
        textViewRemainingBudget = findViewById(R.id.textViewRemainingBudget)
        circularProgressBar = findViewById(R.id.budgetProgressBar)
        textViewWarning = findViewById(R.id.textViewWarning)
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget)
        editTextMonthlyBudget = findViewById(R.id.editTextMonthlyBudget)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)

        // Create notification channel
        createNotificationChannel()

        // Load the current budget from SharedPreferences
        val savedBudget = sharedPreferences.getFloat(budgetKey, 0f)
        textViewCurrentBudget.text = "Current Budget: Rs $savedBudget"

        // Calculate and display current spending
        val totalExpenses = calculateTotalExpensesForCurrentMonth()
        textViewCurrentSpending.text = "Current Spending: Rs $totalExpenses"

        // Calculate and display the remaining budget and check for alerts
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

    // Function to create the notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.budget_alerts_channel_name) // You'll need to define this in strings.xml
            val descriptionText = getString(R.string.budget_alerts_channel_description) // Define this as well
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(BUDGET_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Function to save the budget to SharedPreferences
    fun saveBudget(budget: Float) {
        sharedPreferences.edit().putFloat(budgetKey, budget).apply()
        textViewCurrentBudget.text = "Current Budget: Rs $budget"
        calculateRemainingBudget() // Recalculate and update UI
    }

    // Function to calculate total expenses for the current month
    fun calculateTotalExpensesForCurrentMonth(): Float {
        val allTransactions = loadTransactions()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        var totalExpenses = 0f

        for (transaction in allTransactions) {
            val transactionDateParts = transaction.date.split("-")
            if (transactionDateParts.size == 3) {
                try {
                    val transactionYear = transactionDateParts[0].toInt()
                    val transactionMonth = transactionDateParts[1].toInt() - 1

                    if (transaction.type == "Expense" &&
                        transactionYear == currentYear &&
                        transactionMonth == currentMonth) {
                        totalExpenses += transaction.amount.toFloat()
                    }
                } catch (e: NumberFormatException) {
                    Log.e("BudgetActivity", "Error parsing date: ${transaction.date}", e)
                }
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

    // Function to calculate and update remaining budget and progress bar, and send notifications
    fun calculateRemainingBudget() {
        val savedBudget = sharedPreferences.getFloat(budgetKey, 0f)
        val totalExpenses = calculateTotalExpensesForCurrentMonth()
        val remainingBudget = savedBudget - totalExpenses

        textViewCurrentSpending.text = "Current Spending: Rs $totalExpenses"
        textViewRemainingBudget.text = "Remaining Budget: Rs $remainingBudget"

        val progress = if (savedBudget > 0) {
            ((totalExpenses / savedBudget) * 100).coerceIn(0f, 100f)
        } else {
            0f
        }

        circularProgressBar.apply {
            progressBarColor = Color.parseColor("#2869FE")
            progressBarColorStart = Color.parseColor("#2869FE")
            progressBarColorEnd = Color.parseColor("#1E4C9A")
            progressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM
            backgroundProgressBarColor = Color.parseColor("#A0C8FF")
            backgroundProgressBarColorStart = Color.parseColor("#A0C8FF")
            backgroundProgressBarColorEnd = Color.parseColor("#A0C8FF")
            backgroundProgressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM
            progressBarWidth = 30f
            backgroundProgressBarWidth = 50f
            roundBorder = false
            startAngle = 180f
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
            setProgressWithAnimation(progress, 1000)
        }

        if (remainingBudget < 0) {
            textViewWarning.text = getString(R.string.budget_exceeded_warning) // Define this in strings.xml
            sendBudgetExceededNotification(savedBudget, totalExpenses)
        } else if (savedBudget > 0 && progress >= 80 && progress < 100) {
            sendBudget80PercentExceededNotification(savedBudget, totalExpenses)
            textViewWarning.text = getString(R.string.budget_80_percent_warning) // Define this in strings.xml
        } else {
            textViewWarning.text = ""
        }
    }

    // Function to send a "Budget Exceeded" notification
    private fun sendBudgetExceededNotification(budget: Float, spending: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            val builder = NotificationCompat.Builder(this, BUDGET_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notificationicon)
                .setContentTitle(getString(R.string.budget_exceeded_notification_title))
                .setContentText(getString(R.string.budget_exceeded_notification_message, spending, budget))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                notify(BUDGET_EXCEEDED_NOTIFICATION_ID, builder.build())
            }
        } else {
            Log.w("Budget", "Cannot send budget exceeded notification: Notification permission not granted.");
            // Optionally, inform the user in the UI that notifications are disabled.
        }
    }

    // Function to send a "Budget 80% Exceeded" notification
    private fun sendBudget80PercentExceededNotification(budget: Float, spending: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            val builder = NotificationCompat.Builder(this, BUDGET_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notificationicon)
                .setContentTitle(getString(R.string.budget_80_percent_notification_title))
                .setContentText(getString(R.string.budget_80_percent_notification_message, spending, budget))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                notify(BUDGET_80_PERCENT_NOTIFICATION_ID, builder.build())
            }
        } else {
            Log.w("Budget", "Cannot send 80% budget notification: Notification permission not granted.");
            // Optionally, inform the user in the UI that notifications are disabled.
        }
    }
}