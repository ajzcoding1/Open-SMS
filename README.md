<p align="center"><img width="20%" src="app/data/images/favicont512.png" /></p>

# 📨 OpenSMS
An open-source solution to host a `SMS` API server locally.

### 🤔 Why would you choose our app? (Features)
👉 Turns your `Android` phone into a SMS API server. <br>
👉 Open-source, No advertisements and privacy focused.

## 🧑‍💻 Quickstart Guide

### 📱 Download & Run the Android App
1. You can check the [Releases](https://github.com/ajzcoding1/Open-SMS/releases) and download the latest version of the android app on your phone.
2. You may need to disable (temporarily) `Play Protect` from your `Play Store` settings.
3. Install the App & launch.
4. Grant the permissions.
5. Android might block `sms` permission. Go to `Settings` > `Apps` > `OpenSMS` > Tap on three vertical-dots at top-right corner > `Allow restricted access`.
6. Then grant `SMS` permission from the settings window.
7. Relaunch the app. 
8. Click `Start Server`. It will then prompt you to generate an API Key for the first time. Generate and store it securely.

### 🧪 Test API endpoint
Find the network address of your device and you may test out the API. Sample given below.

```bash
curl -i -X POST 'http://127.0.0.1:9292/sms/' \
    -H 'Content-Type: application/json' \
    -H 'Authorization: Bearer my-secret-api-key' \
    -d '{"number": "+1234567890", "text": "Hello from OpenSMS API!"}'
```

## 📚 Tech Stack Used

### 📱 Native Android Kotlin Stack
- **Native Android:** Kotlin, Material 3, Android Foreground Service with WakeLock.
- **Embedded API Server:** Lightweight NanoHTTPD server on port `9292`.
- **SMS Telephony:** Native `SmsManager` with automatic multi-part message splitting & Dual SIM slot selection.

-----

## 🧩 Building Your App From Code

### 1. Automated Cloud Build (GitHub Actions)
Har push par GitHub Actions automatically APK compile kar deta hai. Aap [Actions tab](https://github.com/ajzcoding1/Open-SMS/actions) se direct fresh APK download kar sakte hain.

### 2. Android Studio (Local PC)
1. Android Studio me `android-kotlin` folder open karein.
2. Gradle sync complete hone dein.
3. Menu me **Build > Build Bundle(s) / APK(s) > Build APK(s)** par click karein.
4. Output APK yahan generate hogi: `android-kotlin/app/build/outputs/apk/debug/app-debug.apk`.
