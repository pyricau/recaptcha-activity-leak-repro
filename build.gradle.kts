plugins {
  id("com.android.application") version "9.4.1" apply false
  // The reCAPTCHA SDK is compiled with Kotlin 2.4, newer than what AGP's built-in Kotlin defaults to.
  id("org.jetbrains.kotlin.android") version "2.4.20" apply false
}
