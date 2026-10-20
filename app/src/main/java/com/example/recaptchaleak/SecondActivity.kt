package com.example.recaptchaleak

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/** An ordinary activity. Once you press back, reCAPTCHA keeps it in memory. */
class SecondActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(TextView(this).apply { text = "Press back, and LeakCanary reports this activity." })
  }
}
