package com.example.recaptchaleak

import android.app.Application
import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.RecaptchaClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async

class ReproApplication : Application() {

  /** Fetched once at startup, with the Application, as the SDK documentation recommends. */
  lateinit var recaptchaClient: Deferred<RecaptchaClient>
    private set

  override fun onCreate() {
    super.onCreate()
    recaptchaClient = MainScope().async { Recaptcha.fetchClient(this@ReproApplication, BuildConfig.RECAPTCHA_SITE_KEY) }
  }
}
