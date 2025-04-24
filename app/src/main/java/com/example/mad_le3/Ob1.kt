package com.example.mad_le3

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Import Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Ob1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ob1)



        // Find the buttons by their IDs
        val skipButton: Button = findViewById(R.id.button_skip_1) // Assuming the ID is button_skip_1
        val nextButton: Button = findViewById(R.id.button)

        // Set OnClickListener for the Skip button
        skipButton.setOnClickListener {
            // Create an Intent to start the HomeActivity
            val intent = Intent(this, Dashboard::class.java) // Replace HomeActivity::class.java with your actual home activity class
            startActivity(intent)
            finish() // Optional: Close Ob1 so the user can't go back to it from Home
        }

        // Set OnClickListener for the Next button
        nextButton.setOnClickListener {
            // Create an Intent to start the Ob2 activity
            val intent = Intent(this, Ob2::class.java) // Assuming your second onboarding activity is named Ob2
            startActivity(intent)
            // Optional: You might not want to finish Ob1 here if you want the user to be able to go back to it from Ob2
        }
    }
}
