package com.quicksms.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.quicksms.app.databinding.ActivityMainBinding
import com.quicksms.app.service.SmsServerService
import com.quicksms.app.util.KeyManager
import com.quicksms.app.util.NetworkUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var keyManager: KeyManager
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val serverReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SmsServerService.ACTION_STATE_CHANGED -> {
                    val isRunning = intent.getBooleanExtra(SmsServerService.EXTRA_IS_RUNNING, false)
                    updateServerUi(isRunning)
                }
                SmsServerService.ACTION_LOG -> {
                    val logMsg = intent.getStringExtra(SmsServerService.EXTRA_LOG_MSG) ?: return
                    appendLog(logMsg)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        if (smsGranted) {
            Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "SMS Permission is required to send messages!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        keyManager = KeyManager(this)

        setupApiKeyUi()
        setupListeners()
        checkAndRequestPermissions()
        updateServerUi(SmsServerService.isRunning)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(SmsServerService.ACTION_STATE_CHANGED)
            addAction(SmsServerService.ACTION_LOG)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serverReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(serverReceiver, filter)
        }

        updateServerUi(SmsServerService.isRunning)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(serverReceiver)
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun setupApiKeyUi() {
        val apiKey = keyManager.getOrCreateApiKey()
        binding.tvApiKey.text = apiKey
    }

    private fun setupListeners() {
        binding.btnServerToggle.setOnClickListener {
            if (!hasSmsPermission()) {
                checkAndRequestPermissions()
                return@setOnClickListener
            }

            if (SmsServerService.isRunning) {
                val stopIntent = Intent(this, SmsServerService::class.java).apply {
                    action = SmsServerService.ACTION_STOP
                }
                startService(stopIntent)
            } else {
                val startIntent = Intent(this, SmsServerService::class.java).apply {
                    action = SmsServerService.ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(startIntent)
                } else {
                    startService(startIntent)
                }
            }
        }

        binding.btnCopyUrl.setOnClickListener {
            copyToClipboard("Gateway URL", binding.tvGatewayUrl.text.toString())
        }

        binding.btnCopyKey.setOnClickListener {
            copyToClipboard("API Key", binding.tvApiKey.text.toString())
        }

        binding.btnRegenerateKey.setOnClickListener {
            val newKey = keyManager.generateNewApiKey()
            binding.tvApiKey.text = newKey
            copyToClipboard("New API Key", newKey)
            appendLog("Generated new API key")
        }

        binding.btnClearLogs.setOnClickListener {
            binding.tvLogs.text = ""
        }
    }

    private fun updateServerUi(isRunning: Boolean) {
        val ip = NetworkUtils.getLocalIpAddress(this)
        val url = "http://$ip:9292"
        binding.tvGatewayUrl.text = url

        if (isRunning) {
            binding.statusDot.setBackgroundResource(R.drawable.circle_green)
            binding.tvStatus.text = getString(R.string.server_running)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_green))

            binding.btnServerToggle.text = getString(R.string.stop_server)
            binding.btnServerToggle.setBackgroundColor(ContextCompat.getColor(this, R.color.status_red))
        } else {
            binding.statusDot.setBackgroundResource(R.drawable.circle_red)
            binding.tvStatus.text = getString(R.string.server_stopped)
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))

            binding.btnServerToggle.text = getString(R.string.start_server)
            binding.btnServerToggle.setBackgroundColor(ContextCompat.getColor(this, R.color.status_green))
        }
    }

    private fun appendLog(message: String) {
        val time = timeFormat.format(Date())
        val currentLogs = binding.tvLogs.text.toString()
        val newEntry = "[$time] $message\n"
        binding.tvLogs.text = newEntry + currentLogs
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndRequestPermissions() {
        val needed = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }
}
