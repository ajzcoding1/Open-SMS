def get_dashboard_html(default_key: str = "qsms_master_key_2026", default_port: int = 9292) -> str:
    return f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickSMS Pro - Web Control Panel</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
  <style>
    :root {{
      --bg: #0d1117;
      --card-bg: rgba(22, 27, 34, 0.85);
      --card-border: rgba(48, 54, 61, 0.8);
      --accent: #2563eb;
      --accent-hover: #1d4ed8;
      --success: #10b981;
      --danger: #ef4444;
      --text: #f0f6fc;
      --text-muted: #8b949e;
      --input-bg: #161b22;
      --input-border: #30363d;
      --code-bg: #0b0e14;
    }}
    * {{
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }}
    body {{
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
      background: radial-gradient(circle at 50% 0%, #1a2333 0%, var(--bg) 70%);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 32px 16px;
    }}
    .container {{
      width: 100%;
      max-width: 860px;
      display: flex;
      flex-direction: column;
      gap: 24px;
    }}
    .header {{
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
    }}
    .brand {{
      display: flex;
      align-items: center;
      gap: 12px;
    }}
    .brand-icon {{
      width: 44px;
      height: 44px;
      background: linear-gradient(135deg, #2563eb, #3b82f6);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      box-shadow: 0 8px 16px rgba(37, 99, 235, 0.3);
    }}
    .brand-title {{
      font-size: 24px;
      font-weight: 800;
      letter-spacing: -0.5px;
    }}
    .brand-title span {{
      color: #60a5fa;
    }}
    .status-badge {{
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: rgba(16, 185, 129, 0.15);
      border: 1px solid rgba(16, 185, 129, 0.4);
      color: #34d399;
      padding: 6px 14px;
      border-radius: 9999px;
      font-size: 13px;
      font-weight: 600;
    }}
    .pulse-dot {{
      width: 8px;
      height: 8px;
      background-color: #34d399;
      border-radius: 50%;
      box-shadow: 0 0 10px #34d399;
      animation: pulse 2s infinite;
    }}
    @keyframes pulse {{
      0% {{ opacity: 1; transform: scale(1); }}
      50% {{ opacity: 0.4; transform: scale(1.2); }}
      100% {{ opacity: 1; transform: scale(1); }}
    }}
    .card {{
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      backdrop-filter: blur(12px);
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
    }}
    .card-title {{
      font-size: 16px;
      font-weight: 700;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #e6edf3;
    }}
    .form-group {{
      margin-bottom: 18px;
    }}
    label {{
      display: block;
      font-size: 13px;
      font-weight: 600;
      color: var(--text-muted);
      margin-bottom: 6px;
    }}
    input, textarea {{
      width: 100%;
      padding: 12px 14px;
      background: var(--input-bg);
      border: 1px solid var(--input-border);
      border-radius: 8px;
      color: var(--text);
      font-family: inherit;
      font-size: 14px;
      transition: all 0.2s;
    }}
    input:focus, textarea:focus {{
      outline: none;
      border-color: var(--accent);
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.25);
    }}
    .key-container {{
      display: flex;
      gap: 8px;
    }}
    .key-container input {{
      font-family: 'JetBrains Mono', monospace;
      font-size: 13px;
    }}
    button {{
      cursor: pointer;
      border: none;
      border-radius: 8px;
      font-weight: 600;
      font-size: 14px;
      transition: all 0.2s;
    }}
    .btn-primary {{
      background: linear-gradient(135deg, #2563eb, #1d4ed8);
      color: white;
      padding: 12px 24px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      width: 100%;
      box-shadow: 0 4px 14px rgba(37, 99, 235, 0.4);
    }}
    .btn-primary:hover {{
      background: linear-gradient(135deg, #1d4ed8, #1e40af);
      transform: translateY(-1px);
    }}
    .btn-secondary {{
      background: #21262d;
      color: var(--text);
      border: 1px solid var(--card-border);
      padding: 12px 16px;
      white-space: nowrap;
    }}
    .btn-secondary:hover {{
      background: #30363d;
    }}
    .response-card {{
      display: none;
      margin-top: 20px;
      padding: 16px;
      border-radius: 10px;
      background: var(--code-bg);
      border: 1px solid var(--input-border);
      font-family: 'JetBrains Mono', monospace;
      font-size: 13px;
      white-space: pre-wrap;
      word-break: break-all;
    }}
    .status-ok {{
      border-left: 4px solid var(--success);
      color: #34d399;
    }}
    .status-err {{
      border-left: 4px solid var(--danger);
      color: #f87171;
    }}
    .code-box {{
      background: var(--code-bg);
      border: 1px solid var(--input-border);
      border-radius: 8px;
      padding: 14px;
      font-family: 'JetBrains Mono', monospace;
      font-size: 12px;
      color: #c9d1d9;
      overflow-x: auto;
      margin-top: 10px;
      position: relative;
    }}
    .copy-btn {{
      position: absolute;
      top: 8px;
      right: 8px;
      font-size: 11px;
      padding: 4px 10px;
      background: #21262d;
      color: var(--text-muted);
      border: 1px solid #30363d;
      border-radius: 6px;
    }}
    .copy-btn:hover {{
      color: var(--text);
      background: #30363d;
    }}
    .footer {{
      text-align: center;
      font-size: 12px;
      color: var(--text-muted);
      margin-top: 16px;
    }}
    .footer a {{
      color: #60a5fa;
      text-decoration: none;
    }}
  </style>
</head>
<body>

  <div class="container">
    <div class="header">
      <div class="brand">
        <div class="brand-icon">⚡</div>
        <div>
          <div class="brand-title">QuickSMS <span>Pro</span></div>
          <div style="font-size: 12px; color: var(--text-muted);">SMS Gateway Server</div>
        </div>
      </div>
      <div class="status-badge">
        <div class="pulse-dot"></div>
        Server Online
      </div>
    </div>

    <!-- API Key Section -->
    <div class="card">
      <div class="card-title">🔑 API Key Authentication</div>
      <div class="form-group">
        <label for="apiKey">Active Secret Token (Pre-configured):</label>
        <div class="key-container">
          <input type="text" id="apiKey" value="{default_key}" spellcheck="false" />
          <button class="btn-secondary" onclick="copyKey()">📋 Copy</button>
        </div>
        <div style="font-size: 11px; color: #34d399; margin-top: 6px;">
          ✓ Automatically configured in your database for seamless authorization.
        </div>
      </div>
    </div>

    <!-- Send SMS Form -->
    <div class="card">
      <div class="card-title">📱 Send SMS Message</div>
      
      <div class="form-group">
        <label for="phoneNumber">Recipient Phone Number (with Country Code):</label>
        <input type="text" id="phoneNumber" placeholder="+919876543210" value="+91" />
      </div>

      <div class="form-group">
        <label for="messageText">SMS Message Text:</label>
        <textarea id="messageText" rows="3" placeholder="Enter your text message here...">Hello from QuickSMS Pro Gateway!</textarea>
      </div>

      <button id="sendBtn" class="btn-primary" onclick="dispatchSms()">
        <span>🚀 Send SMS Now</span>
      </button>

      <div id="responseBox" class="response-card"></div>
    </div>

    <!-- Quick Integration Snippets -->
    <div class="card">
      <div class="card-title">⚡ Quick Code Snippets</div>
      
      <label>cURL Command:</label>
      <div class="code-box">
        <button class="copy-btn" onclick="copyCode('curlCode')">Copy</button>
        <span id="curlCode">curl -i -X POST 'http://127.0.0.1:{default_port}/sms/' \\
  -H 'Content-Type: application/json' \\
  -H 'Authorization: Bearer {default_key}' \\
  -d '{{"number": "+919876543210", "text": "Hello from QuickSMS Pro!"}}'</span>
      </div>

      <label style="margin-top: 14px;">Python (requests):</label>
      <div class="code-box">
        <button class="copy-btn" onclick="copyCode('pyCode')">Copy</button>
        <span id="pyCode">import requests

url = "http://127.0.0.1:{default_port}/sms/"
headers = {{
    "Authorization": "Bearer {default_key}",
    "Content-Type": "application/json"
}}
data = {{"number": "+919876543210", "text": "Hello from QuickSMS Pro!"}}

res = requests.post(url, json=data, headers=headers)
print(res.json())</span>
      </div>
    </div>

    <div class="footer">
      QuickSMS Pro Server running on <b>http://127.0.0.1:{default_port}</b> | Interactive OpenAPI Docs: <a href="/docs" target="_blank">/docs</a>
    </div>
  </div>

  <script>
    function copyKey() {{
      const keyInput = document.getElementById('apiKey');
      navigator.clipboard.writeText(keyInput.value);
      alert('API Key copied to clipboard!');
    }}

    function copyCode(id) {{
      const text = document.getElementById(id).innerText;
      navigator.clipboard.writeText(text);
      alert('Code snippet copied!');
    }}

    async function dispatchSms() {{
      const sendBtn = document.getElementById('sendBtn');
      const responseBox = document.getElementById('responseBox');
      const apiKey = document.getElementById('apiKey').value.trim();
      const phone = document.getElementById('phoneNumber').value.trim();
      const text = document.getElementById('messageText').value.trim();

      if (!phone || phone === '+91' || phone.length < 5) {{
        alert('Please enter a valid phone number with country code!');
        return;
      }}

      if (!text) {{
        alert('Please enter message text!');
        return;
      }}

      sendBtn.disabled = true;
      sendBtn.innerHTML = '<span>⏳ Sending SMS...</span>';
      responseBox.style.display = 'none';

      try {{
        const res = await fetch('/sms/', {{
          method: 'POST',
          headers: {{
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + apiKey
          }},
          body: JSON.stringify({{
            number: phone,
            text: text
          }})
        }});

        const data = await res.json();
        responseBox.style.display = 'block';

        if (res.ok) {{
          responseBox.className = 'response-card status-ok';
          responseBox.innerHTML = '<b>✓ 200 OK - SMS Request Accepted</b>\\n' + JSON.stringify(data, null, 2);
        }} else {{
          responseBox.className = 'response-card status-err';
          responseBox.innerHTML = '<b>✗ ' + res.status + ' ' + (res.statusText || 'Error') + '</b>\\n' + JSON.stringify(data, null, 2);
        }}
      }} catch (err) {{
        responseBox.style.display = 'block';
        responseBox.className = 'response-card status-err';
        responseBox.innerText = 'Connection Error: ' + err.message;
      }} finally {{
        sendBtn.disabled = false;
        sendBtn.innerHTML = '<span>🚀 Send SMS Now</span>';
      }}
    }}
  </script>
</body>
</html>
"""
