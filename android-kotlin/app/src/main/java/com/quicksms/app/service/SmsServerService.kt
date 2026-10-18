package com.quicksms.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.quicksms.app.MainActivity
import com.quicksms.app.sms.SmsDispatcher
import com.quicksms.app.util.KeyManager
import com.quicksms.app.util.NetworkUtils
import java.io.IOException

class SmsServerService : Service() {

    companion object {
        const val CHANNEL_ID = "quicksms_server_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.quicksms.app.action.START"
        const val ACTION_STOP = "com.quicksms.app.action.STOP"
        const val ACTION_LOG = "com.quicksms.app.broadcast.LOG"
        const val ACTION_STATE_CHANGED = "com.quicksms.app.broadcast.STATE"

        const val EXTRA_LOG_MSG = "extra_log_msg"
        const val EXTRA_IS_ERROR = "extra_is_error"
        const val EXTRA_IS_RUNNING = "extra_is_running"

        var isRunning: Boolean = false
            private set
    }

    private var server: HttpServer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): SmsServerService = this@SmsServerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopServer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                startServer()
            }
        }
        return START_STICKY
    }

    private fun startServer() {
        if (isRunning) return

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "QuickSMS::CpuWakeLock").apply {
                acquire(10 * 60 * 60 * 1000L)
            }

            val keyManager = KeyManager(this)
            val smsDispatcher = SmsDispatcher(this)

            server = HttpServer(
                port = 9292,
                keyManager = keyManager,
                smsDispatcher = smsDispatcher
            ) { message, isError ->
                broadcastLog(message, isError)
            }

            server?.start()
            isRunning = true

            val ip = NetworkUtils.getLocalIpAddress(this)
            startForeground(NOTIFICATION_ID, buildNotification("QuickSMS Gateway: http://$ip:9292"))
            broadcastState(true)
            broadcastLog("QuickSMS Server started on port 9292 (IP: $ip)", false)

        } catch (e: IOException) {
            Log.e("SmsServerService", "Failed to start server: ${e.message}", e)
            broadcastLog("Server error: ${e.message}", true)
            stopSelf()
        }
    }

    private fun stopServer() {
        try {
            server?.stop()
            server = null
            isRunning = false

            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null

            broadcastState(false)
            broadcastLog("QuickSMS Server stopped", false)
        } catch (e: Exception) {
            Log.e("SmsServerService", "Error stopping server: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    private fun broadcastLog(message: String, isError: Boolean) {
        val intent = Intent(ACTION_LOG).apply {
            putExtra(EXTRA_LOG_MSG, message)
            putExtra(EXTRA_IS_ERROR, isError)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun broadcastState(running: Boolean) {
        val intent = Intent(ACTION_STATE_CHANGED).apply {
            putExtra(EXTRA_IS_RUNNING, running)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun buildNotification(statusText: String): Notification {
        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingMain = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, SmsServerService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QuickSMS Pro Gateway")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingMain)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Server", pendingStop)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "QuickSMS Server Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status while QuickSMS gateway server is running"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
