package com.example.mad_le3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mad_le3.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class Settings : AppCompatActivity() {

    private val backupFileNameInternal = "transaction_backup.json"
    private val sharedPrefsKey = "transactions_list"
    private val gson = Gson()
    private lateinit var backbtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backbtn = findViewById(R.id.imageView)

        val backupCardView = findViewById<com.google.android.material.card.MaterialCardView>(R.id.layoutBackup)
        val restoreCardView = findViewById<com.google.android.material.card.MaterialCardView>(R.id.layoutRestore)

        backupCardView.setOnClickListener {
            backupTransactionsInternal()
        }

        restoreCardView.setOnClickListener {
            restoreTransactionsInternal()
        }

        backbtn.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }
    }

    private fun backupTransactionsInternal() {
        val sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)
        val transactionsJson = sharedPreferences.getString(sharedPrefsKey, null)

        if (transactionsJson != null) {
            try {
                val fileOutputStream: FileOutputStream =
                    openFileOutput(backupFileNameInternal, Context.MODE_PRIVATE)
                fileOutputStream.write(transactionsJson.toByteArray())
                fileOutputStream.close()
                Toast.makeText(this, "Internal backup successful", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(this, "Internal backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "No transactions to backup", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restoreTransactionsInternal() {
        val file = File(filesDir, backupFileNameInternal)
        if (file.exists()) {
            try {
                val fileInputStream: FileInputStream = openFileInput(backupFileNameInternal)
                val buffer = ByteArray(fileInputStream.available())
                fileInputStream.read(buffer)
                fileInputStream.close()
                val transactionsJson = String(buffer, Charsets.UTF_8)

                val typeToken = object : TypeToken<List<Transaction>>() {}.type
                val restoredTransactions: List<Transaction> = gson.fromJson(transactionsJson, typeToken)

                val sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString(sharedPrefsKey, gson.toJson(restoredTransactions))
                editor.apply()

                Toast.makeText(this, "Internal restore successful", Toast.LENGTH_SHORT).show()
                // Optionally, notify the user or update the UI to reflect the restored data
            } catch (e: IOException) {
                Toast.makeText(this, "Internal restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "No internal backup file found", Toast.LENGTH_SHORT).show()
        }
    }
}