plugins {
  id("com.android.application")
}

android {
  namespace = "com.example.recaptchaleak"
  compileSdk = 36

  defaultConfig {
    // The SDK only watches activities when the site key allows the app's package name.
    applicationId = providers.gradleProperty("applicationId").getOrElse("com.example.recaptchaleak")
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // ./gradlew connectedDebugAndroidTest -PrecaptchaSiteKey=<key> [-PapplicationId=<package>]
    val siteKey = providers.gradleProperty("recaptchaSiteKey").getOrElse("")
    buildConfigField("String", "RECAPTCHA_SITE_KEY", "\"$siteKey\"")
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation("com.google.android.recaptcha:recaptcha:18.9.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

  // Reports the leak when you open SecondActivity and press back.
  debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

  androidTestImplementation("androidx.test:core:1.7.0")
  androidTestImplementation("androidx.test:runner:1.7.0")
  androidTestImplementation("androidx.test.ext:junit:1.3.0")
  androidTestImplementation("com.squareup.leakcanary:leakcanary-android-instrumentation:2.14")
}
