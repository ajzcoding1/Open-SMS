# 📱 QuickSMS Pro - Native Android Kotlin App

A lightweight, high-performance native **Kotlin** implementation of the SMS Gateway Server for Android phones.

- **App Name:** QuickSMS Pro
- **Package Name:** `com.quicksms.app`
- **Architecture:** Android Foreground Service + Embedded NanoHTTPD REST API + Native SmsManager
- **Default Port:** `9292`

---

## 🌟 Key Features

1. **Lightweight (< 4 MB):** Ultra-fast startup with zero bloat.
2. **True 24/7 Background Service:** Runs as an Android Foreground Service with `WAKE_LOCK` and persistent notification, ensuring Android never kills the gateway when the screen locks.
3. **Native SMS Dispatcher:** Directly calls Android's `SmsManager` with automatic multi-part message splitting for long SMS.
4. **Interactive Web Control Panel:** Embedded web panel at `http://<phone-ip>:9292/` with pre-filled API Key for instant browser-based SMS sending.
5. **Modern Material 3 UI:** Clean dark theme with live server status, 1-tap copy, and real-time request logs.

---

## 🛠️ How to Open & Build the APK

1. Open **Android Studio**.
2. Select **File > Open...** and choose the `android-kotlin` folder:
   `c:\Users\lamga\Desktop\OpenSMS\android-kotlin`
3. Wait for Gradle sync to complete.
4. Connect your Android phone via USB (with USB Debugging enabled).
5. Click **Run (▶)** to install directly, or click **Build > Build Bundle(s) / APK(s) > Build APK(s)** to export your APK file!

---

## 🔌 API Specification

### Endpoint: `POST /sms/`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer <API-KEY>`
- **Body:**
  ```json
  {
    "number": "+919876543210",
    "text": "Hello from QuickSMS Pro Gateway!"
  }
  ```
- **Response:**
  ```json
  {
    "stat": true
  }
  ```

### Endpoint: `GET /` or `GET /panel`
- Serves the interactive Web Control Panel in any browser.
