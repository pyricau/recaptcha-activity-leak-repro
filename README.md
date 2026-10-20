# reCAPTCHA Android SDK: destroyed activities leak

A minimal app that reproduces a memory leak in the reCAPTCHA Enterprise Android SDK,
`com.google.android.recaptcha:recaptcha`, from 18.9.0 to 18.9.3, the latest release.

After `Recaptcha.fetchClient()`, the SDK keeps the window of the last created activity after that
activity is destroyed. The window references the activity, so the activity and its whole view
hierarchy stay in memory until another activity is created.

## Reproduce

You need a reCAPTCHA site key that allows this app's package name, `com.example.recaptchaleak`. If
the key doesn't allow it, the SDK never starts watching activities, and the test fails with
"reCAPTCHA never started watching activities". To use a key you already have, build the app under
a package name that key allows, with `-PapplicationId=<package>`.

With an emulator or device connected:

```
./gradlew connectedDebugAndroidTest -PrecaptchaSiteKey=<site key>
```

[`RecaptchaActivityLeakTest`](app/src/androidTest/java/com/example/recaptchaleak/RecaptchaActivityLeakTest.kt)
does the following:

1. Waits for the client that
   [`ReproApplication`](app/src/main/java/com/example/recaptchaleak/ReproApplication.kt) fetches at
   startup.
2. Opens `MainActivity`, then opens and closes `SecondActivity`. `SecondActivity` is an ordinary
   activity: it is resumed, paused, stopped, then destroyed.
3. Asserts, with LeakCanary, that nothing leaked.

The assertion fails:

```
┬───
│ GC Root: System class
│
├─ android.provider.FontsContract class
│    Leaking: NO (ReproApplication↓ is not leaking and a class is never leaking)
│    ↓ static FontsContract.sContext
├─ com.example.recaptchaleak.ReproApplication instance
│    Leaking: NO (Application is a singleton)
│    mBase instance of android.app.ContextImpl
│    ↓ Application.mActivityLifecycleCallbacks
│                  ~~~~~~~~~~~~~~~~~~~~~~~~~~~
├─ java.util.ArrayList instance
│    Leaking: UNKNOWN
│    Retaining 300 B in 16 objects
│    ↓ ArrayList[4]
│               ~~~
├─ com.google.android.recaptcha.internal.zzas instance
│    Leaking: UNKNOWN
│    Retaining 469.4 kB in 1204 objects
│    ↓ zzas.zzb
│           ~~~
├─ com.google.android.recaptcha.internal.zzbd instance
│    Leaking: UNKNOWN
│    Retaining 469.4 kB in 1202 objects
│    ↓ zzbd.zza
│           ~~~
├─ com.android.internal.policy.PhoneWindow instance
│    Leaking: YES (Window#mDestroyed is true)
│    Retaining 469.3 kB in 1200 objects
│    mContext instance of com.example.recaptchaleak.SecondActivity with mDestroyed = true
│    mOnWindowDismissedCallback instance of com.example.recaptchaleak.SecondActivity with mDestroyed = true
│    ↓ Window.mContext
╰→ com.example.recaptchaleak.SecondActivity instance
     Leaking: YES (ObjectWatcher was watching this because com.example.recaptchaleak.SecondActivity received Activity#onDestroy() callback and Activity#mDestroyed is true)
     Retaining 2.1 kB in 30 objects
```

You can also see the leak by hand. Install the debug build, tap "Open SecondActivity", press back,
and LeakCanary reports `SecondActivity` a few seconds later.

## Cause

The class names below are from 18.9.3, decompiled.

- The client (`zzgu`) creates a `zzbb`, which registers an `ActivityLifecycleCallbacks`, `zzas`. It
  never unregisters it.
- `zzas.onActivityCreated()` sets `zzb = new zzbd(activity.getWindow(), activity.getClass().getSimpleName(), …)`.
  It then calls `zzb.zza()`, which wraps the window's `Window.Callback` to record touch events.
- `zzas.onActivityDestroyed()` only unregisters its fragment callbacks. It never clears `zzb`, and
  nothing reads `zzb` again.

The fragment version of the same code, `zzat`, does clear its `zzbd` in `onFragmentDestroyed()`.

## Suggested fix

In `zzas.onActivityDestroyed()`, set `zzb` to null. Better still, make it a local variable, since
the wrapper installed on the window already keeps the `zzbd` for as long as the window lives.
