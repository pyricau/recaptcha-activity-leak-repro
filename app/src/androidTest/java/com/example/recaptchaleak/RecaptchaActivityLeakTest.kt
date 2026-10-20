package com.example.recaptchaleak

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import leakcanary.LeakAssertions
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecaptchaActivityLeakTest {

  @Test
  fun destroyedActivityIsNotRetained() {
    val application = ApplicationProvider.getApplicationContext<ReproApplication>()
    runBlocking { application.recaptchaClient.await() }

    ActivityScenario.launch(MainActivity::class.java).use {
      // Resumed, paused, stopped then destroyed: the usual lifecycle of a screen you leave.
      openAndCloseSecondActivityUntilRecaptchaWatchesIt()

      // Fails: Application.mActivityLifecycleCallbacks -> zzas.zzb -> zzbd.zza (PhoneWindow) ->
      // SecondActivity, mDestroyed = true.
      LeakAssertions.assertNoLeaks()
    }
  }

  /**
   * The SDK registers its activity lifecycle callbacks a moment after fetchClient() returns, so
   * this retries until the SDK has seen SecondActivity created. It wraps the window callback then.
   */
  private fun openAndCloseSecondActivityUntilRecaptchaWatchesIt() {
    repeat(20) {
      var seenByRecaptcha = false
      ActivityScenario.launch(SecondActivity::class.java).use { scenario ->
        scenario.onActivity { activity ->
          seenByRecaptcha =
            activity.window.callback.javaClass.name.startsWith("com.google.android.recaptcha.")
        }
      }
      if (seenByRecaptcha) {
        return
      }
      Thread.sleep(500)
    }
    error("reCAPTCHA never started watching activities")
  }
}
