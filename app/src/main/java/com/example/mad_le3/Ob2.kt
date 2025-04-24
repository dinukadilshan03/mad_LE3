package com.example.mad_le3

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Import Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Ob2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ob2) // Assumes your layout file for Ob2 is activity_ob2.xml



        // Find the buttons by their IDs in the activity_ob2.xml layout
        // Make sure these IDs match the ones in your actual activity_ob2.xml
        val skipButton: Button = findViewById(R.id.button_skip_2) // Assuming the Skip button ID is button_skip_2
        val nextButton: Button = findViewById(R.id.button_next_2) // Assuming the Next button ID is button_next_2

        // Set OnClickListener for the Skip button
        skipButton.setOnClickListener {
            // Create an Intent to start the HomeActivity
            val intent = Intent(this, Dashboard::class.java) // Replace HomeActivity::class.java with your actual home activity class
            startActivity(intent)
            finish() // Close Ob2 so the user can't go back to it from Home
        }

        // Set OnClickListener for the Next button
        nextButton.setOnClickListener {
            // Create an Intent to start the Ob3 activity
            val intent = Intent(this, Ob3::class.java) // Assuming your third onboarding activity is named Ob3
            startActivity(intent)
            // Optional: You might not want to finish Ob2 here if you want the user to be able to go back to it from Ob3
        }
    }
}
