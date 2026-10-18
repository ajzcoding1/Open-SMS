package com.quicksms.app.service

import com.google.gson.Gson
import fi.iki.elonen.NanoHTTPD
import com.quicksms.app.model.SmsRequest
import com.quicksms.app.model.SmsResponse
import com.quicksms.app.sms.SmsDispatcher
import com.quicksms.app.util.KeyManager

class HttpServer(
    port: Int = 9292,
    private val keyManager: KeyManager,
    private val smsDispatcher: SmsDispatcher,
    private val onLog: (message: String, isError: Boolean) -> Unit
) : NanoHTTPD("0.0.0.0", port) {

    private val gson = Gson()

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        if (method == Method.OPTIONS) {
            val response = newFixedLengthResponse(Response.Status.OK, "text/plain", "")
            addCorsHeaders(response)
            return response
        }

        if (method == Method.GET && (uri == "/" || uri == "/panel")) {
            val activeKey = keyManager.getOrCreateApiKey()
            val html = getDashboardHtml(activeKey, listeningPort)
            val response = newFixedLengthResponse(Response.Status.OK, "text/html", html)
            addCorsHeaders(response)
            return response
        }

        if (method == Method.GET && uri == "/api/status") {
            val json = gson.toJson(mapOf("app" to "QuickSMS Pro", "status" to "running", "port" to listeningPort))
            val response = newFixedLengthResponse(Response.Status.OK, "application/json", json)
            addCorsHeaders(response)
            return response
        }

        if (method == Method.POST && (uri == "/sms/" || uri == "/sms")) {
            val authHeader = session.headers["authorization"]
            val clientIp = session.remoteIpAddress ?: "unknown"

            if (!keyManager.isValidToken(authHeader)) {
                onLog("Auth failed from IP: $clientIp", true)
                val errJson = gson.toJson(mapOf("detail" to "Invalid or missing API key"))
                val response = newFixedLengthResponse(Response.Status.UNAUTHORIZED, "application/json", errJson)
                addCorsHeaders(response)
                return response
            }

            val postFiles = HashMap<String, String>()
            try {
                session.parseBody(postFiles)
            } catch (e: Exception) {
                onLog("Error parsing body from $clientIp: ${e.message}", true)
            }

            val postData = postFiles["postData"] ?: ""
            if (postData.isBlank()) {
                val errJson = gson.toJson(mapOf("detail" to "Empty request body"))
                val response = newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", errJson)
                addCorsHeaders(response)
                return response
            }

            return try {
                val req = gson.fromJson(postData, SmsRequest::class.java)
                onLog("Sending SMS to ${req.number} from $clientIp", false)
                
                val success = smsDispatcher.sendSms(req.number, req.text, req.simSlot)
                if (success) {
                    onLog("✓ SMS delivered to ${req.number}", false)
                } else {
                    onLog("✗ SMS failed to ${req.number}", true)
                }

                val responseJson = gson.toJson(SmsResponse(stat = success))
                val response = newFixedLengthResponse(Response.Status.OK, "application/json", responseJson)
                addCorsHeaders(response)
                response
            } catch (e: Exception) {
                onLog("JSON Parse error: ${e.message}", true)
                val errJson = gson.toJson(mapOf("detail" to "Malformed JSON payload: ${e.message}"))
                val response = newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", errJson)
                addCorsHeaders(response)
                response
            }
        }

        val notFoundJson = gson.toJson(mapOf("detail" to "Not Found"))
        val response = newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", notFoundJson)
        addCorsHeaders(response)
        return response
    }

    private fun addCorsHeaders(response: Response) {
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
    }

    private fun getDashboardHtml(defaultKey: String, port: Int): String {
        return """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickSMS Pro - Control Panel</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #0d1117;
      --card-bg: rgba(22, 27, 34, 0.85);
      --card-border: rgba(48, 54, 61, 0.8);
      --accent: #2563eb;
      --success: #10b981;
      --danger: #ef4444;
      --text: #f0f6fc;
      --text-muted: #8b949e;
      --input-bg: #161b22;
      --input-border: #30363d;
      --code-bg: #0b0e14;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Inter', sans-serif;
      background: radial-gradient(circle at 50% 0%, #1e293b 0%, var(--bg) 70%);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24px 16px;
    }
    .container { width: 100%; max-width: 720px; display: flex; flex-direction: column; gap: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; }
    .brand { display: flex; align-items: center; gap: 12px; }
    .brand-icon {
      width: 44px; height: 44px; background: linear-gradient(135deg, #2563eb, #3b82f6);
      border-radius: 12px; display: flex; align-items: center; justify-content: center;
      font-size: 22px; box-shadow: 0 8px 16px rgba(37, 99, 235, 0.3);
    }
    .brand-title { font-size: 22px; font-weight: 800; }
    .status-badge {
      display: inline-flex; align-items: center; gap: 8px;
      background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.4);
      color: #34d399; padding: 6px 14px; border-radius: 9999px; font-size: 13px; font-weight: 600;
    }
    .pulse-dot { width: 8px; height: 8px; background-color: #34d399; border-radius: 50%; box-shadow: 0 0 10px #34d399; }
    .card {
      background: var(--card-bg); border: 1px solid var(--card-border);
      backdrop-filter: blur(12px); border-radius: 16px; padding: 22px;
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
    }
    .card-title { font-size: 16px; font-weight: 700; margin-bottom: 14px; color: #e6edf3; }
    .form-group { margin-bottom: 16px; }
    label { display: block; font-size: 13px; font-weight: 600; color: var(--text-muted); margin-bottom: 6px; }
    input, textarea {
      width: 100%; padding: 12px 14px; background: var(--input-bg);
      border: 1px solid var(--input-border); border-radius: 8px; color: var(--text);
      font-family: inherit; font-size: 14px;
    }
    input:focus, textarea:focus { outline: none; border-color: var(--accent); }
    .btn-primary {
      background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white;
      padding: 12px 24px; border: none; border-radius: 8px; font-weight: 600; font-size: 15px;
      width: 100%; cursor: pointer;
    }
    .response-card {
      display: none; margin-top: 16px; padding: 14px; border-radius: 8px;
      background: var(--code-bg); font-family: 'JetBrains Mono', monospace; font-size: 13px;
    }
    .status-ok { border-left: 4px solid var(--success); color: #34d399; }
    .status-err { border-left: 4px solid var(--danger); color: #f87171; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div class="brand">
        <div class="brand-icon">⚡</div>
        <div>
          <div class="brand-title">QuickSMS Pro</div>
          <div style="font-size: 12px; color: var(--text-muted);">Native Kotlin SMS Gateway</div>
        </div>
      </div>
      <div class="status-badge"><div class="pulse-dot"></div> Online (Port $port)</div>
    </div>

    <div class="card">
      <div class="card-title">🔑 API Key Authentication</div>
      <div class="form-group">
        <label>Active Secret Key (Pre-filled):</label>
        <input type="text" id="apiKey" value="$defaultKey" readonly />
      </div>
    </div>

    <div class="card">
      <div class="card-title">✉️ Send SMS Message (From Phone's SIM)</div>
      <div class="form-group">
        <label>Recipient Phone Number (e.g. +919876543210):</label>
        <input type="text" id="phoneNumber" placeholder="+91" value="+91" />
      </div>
      <div class="form-group">
        <label>SMS Text:</label>
        <textarea id="messageText" rows="3" placeholder="Enter message text...">Hello from QuickSMS Pro Gateway!</textarea>
      </div>
      <button class="btn-primary" id="sendBtn" onclick="dispatchSms()">🚀 Send SMS Now</button>
      <div id="responseBox" class="response-card"></div>
    </div>
  </div>

  <script>
    async function dispatchSms() {
      const sendBtn = document.getElementById('sendBtn');
      const box = document.getElementById('responseBox');
      const phone = document.getElementById('phoneNumber').value.trim();
      const text = document.getElementById('messageText').value.trim();
      const key = document.getElementById('apiKey').value.trim();

      if (!phone || phone.length < 5) { alert('Enter valid phone number!'); return; }
      if (!text) { alert('Enter message!'); return; }

      sendBtn.disabled = true;
      sendBtn.innerText = 'Sending SMS...';
      box.style.display = 'none';

      try {
        const res = await fetch('/sms/', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + key
          },
          body: JSON.stringify({ number: phone, text: text })
        });
        const data = await res.json();
        box.style.display = 'block';
        if (res.ok && data.stat) {
          box.className = 'response-card status-ok';
          box.innerText = '✓ 200 OK: SMS successfully sent via Phone SIM!\n' + JSON.stringify(data, null, 2);
        } else {
          box.className = 'response-card status-err';
          box.innerText = '✗ Error: ' + JSON.stringify(data, null, 2);
        }
      } catch (e) {
        box.style.display = 'block';
        box.className = 'response-card status-err';
        box.innerText = 'Failed: ' + e.message;
      } finally {
        sendBtn.disabled = false;
        sendBtn.innerText = '🚀 Send SMS Now';
      }
    }
  </script>
</body>
</html>"""
    }
}
