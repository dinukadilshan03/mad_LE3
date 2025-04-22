package com.example.mad_le3

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mad_le3.databinding.ActivityBackupRestoreBinding
import com.example.mad_le3.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

class BackupRestore : AppCompatActivity() {

    private lateinit var binding: ActivityBackupRestoreBinding
    private val gson = Gson()
    private val backupFileNameInternal = "transaction_backup.json"
    private val exportFileNameJson = "transactions_export.json"
    private val sharedPrefsKey = "transactions_list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBackup.setOnClickListener {
            backupTransactionsInternal()
        }

        binding.buttonRestore.setOnClickListener {
            restoreTransactionsInternal()
        }

        binding.buttonExportJson.setOnClickListener {
            exportTransactionsToJsonMediaStore()
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

    private fun exportTransactionsToJsonMediaStore() {
        val sharedPreferences = getSharedPreferences("MyFinanceApp", Context.MODE_PRIVATE)
        val transactionsJson = sharedPreferences.getString(sharedPrefsKey, null)

        if (transactionsJson != null) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, exportFileNameJson)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                } else {
                    @Suppress("DEPRECATION")
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    @Suppress("DEPRECATION")
                    val exportFile = File(downloadsDir, exportFileNameJson)
                    put(MediaStore.MediaColumns.DATA, exportFile.absolutePath)
                }
            }

            val resolver = contentResolver
            val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

            uri?.let {
                try {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(transactionsJson)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                    }

                    Toast.makeText(this, "Transactions exported to Downloads/${exportFileNameJson}", Toast.LENGTH_LONG).show()

                } catch (e: IOException) {
                    resolver.delete(uri, null, null) // Clean up if error
                    Toast.makeText(this, "JSON export failed: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } ?: run {
                Toast.makeText(this, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show()
        }
    }
}