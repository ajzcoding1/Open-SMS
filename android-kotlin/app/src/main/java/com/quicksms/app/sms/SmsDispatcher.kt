package com.quicksms.app.sms

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.util.Log

class SmsDispatcher(private val context: Context) {

    companion object {
        private const val TAG = "QuickSMS-Dispatcher"
    }

    fun sendSms(destination: String, text: String): Boolean {
        return try {
            val cleanPhone = destination.trim()
            if (cleanPhone.isEmpty() || text.isEmpty()) {
                Log.e(TAG, "Destination or message text is empty")
                return false
            }

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val messageParts = smsManager.divideMessage(text)
            if (messageParts.size > 1) {
                Log.d(TAG, "Sending multipart SMS (${messageParts.size} parts) to $cleanPhone")
                smsManager.sendMultipartTextMessage(cleanPhone, null, messageParts, null, null)
            } else {
                Log.d(TAG, "Sending single SMS to $cleanPhone")
                smsManager.sendTextMessage(cleanPhone, null, text, null, null)
            }

            Log.i(TAG, "SMS successfully dispatched to: $cleanPhone")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS: ${e.message}", e)
            false
        }
    }
}
