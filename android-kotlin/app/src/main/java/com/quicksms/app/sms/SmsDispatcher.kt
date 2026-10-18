package com.quicksms.app.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat

class SmsDispatcher(private val context: Context) {

    companion object {
        private const val TAG = "QuickSMS-Dispatcher"
    }

    fun sendSms(destination: String, text: String, simSlot: Int? = null): Boolean {
        return try {
            val cleanPhone = destination.trim()
            if (cleanPhone.isEmpty() || text.isEmpty()) {
                Log.e(TAG, "Destination or message text is empty")
                return false
            }

            var smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            // Dual SIM handling if simSlot is specified (0 for SIM 1, 1 for SIM 2)
            if (simSlot != null && simSlot >= 0) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                        val activeSubs = subManager?.activeSubscriptionInfoList
                        if (!activeSubs.isNullOrEmpty() && simSlot < activeSubs.size) {
                            val subId = activeSubs[simSlot].subscriptionId
                            Log.d(TAG, "Using SIM Slot $simSlot (Subscription ID: $subId)")
                            smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                smsManager.createForSubscriptionId(subId)
                            } else {
                                @Suppress("DEPRECATION")
                                SmsManager.getSmsManagerForSubscriptionId(subId)
                            }
                        } else {
                            Log.w(TAG, "Requested SIM slot $simSlot not available, falling back to default SIM")
                        }
                    } catch (se: SecurityException) {
                        Log.w(TAG, "READ_PHONE_STATE permission denied for Dual SIM selection, using default SIM: ${se.message}")
                    }
                }
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
