package com.example.mad_le3

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


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
    }
}