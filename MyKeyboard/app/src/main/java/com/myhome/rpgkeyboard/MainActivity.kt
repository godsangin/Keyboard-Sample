package com.myhome.rpgkeyboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settingHeader = findViewById<ConstraintLayout>(R.id.setting_header)
        val submitButton = settingHeader.findViewById<TextView>(R.id.submit_text)
        submitButton.visibility = View.GONE


    }




}
