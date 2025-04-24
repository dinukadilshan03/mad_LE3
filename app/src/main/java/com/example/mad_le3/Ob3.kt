package com.example.mad_le3

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Import Button
import android.view.View // Import View for visibility check
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Ob3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ob3) // Assumes your layout file for Ob3 is activity_ob3.xml



        // Find the buttons by their IDs in the activity_ob3.xml layout
        // Make sure these IDs match the ones in your actual activity_ob3.xml
        val skipButton: Button? = findViewById(R.id.button_skip_3) // Skip button might be invisible or gone
        val finishButton: Button = findViewById(R.id.button_finish_3) // Assuming the Finish button ID is button_finish_3

        // Set OnClickListener for the Skip button (handle if it exists and is visible)
        skipButton?.setOnClickListener {
            // Create an Intent to start the HomeActivity
            val intent = Intent(this, Dashboard::class.java) // Replace HomeActivity::class.java with your actual home activity class
            startActivity(intent)
            finish() // Close Ob3 so the user can't go back to it from Home
        }

        // Set OnClickListener for the Finish button
        finishButton.setOnClickListener {
            // Create an Intent to start the HomeActivity
            val intent = Intent(this, Dashboard::class.java) // Replace HomeActivity::class.java with your actual home activity class
            startActivity(intent)
            finish() // Close Ob3 so the user can't go back to it from Home
        }
    }
}
