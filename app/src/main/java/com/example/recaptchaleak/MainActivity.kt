package com.example.recaptchaleak

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(
      Button(this).apply {
        text = "Open SecondActivity"
        setOnClickListener { startActivity(Intent(this@MainActivity, SecondActivity::class.java)) }
      }
    )
  }
}
