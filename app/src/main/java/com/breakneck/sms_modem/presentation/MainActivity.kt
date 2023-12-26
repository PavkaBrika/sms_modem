package com.breakneck.sms_modem.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.breakneck.sms_modem.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val vm = ViewModelProvider(this,)

        val activateButton: Button = findViewById(R.id.activateServiceButton)
        activateButton.setOnClickListener {

        }
    }
}