# 📨 OpenSMS - Complete Guide & Setup Manual (Hinglish)

> **Ek simple guide:** OpenSMS kya hai, iska kya kaam hai, aur ise apne Mobile aur PC par step-by-step kaise setup aur use karein.

---

## 📌 1. OpenSMS Kya Hai aur Iska Kya Kaam Hai?

**OpenSMS** ek open-source system hai jo aapke **Android phone ko ek SMS API Server (Gateway)** bana deta hai.

### 🤔 Iska Asli Kaam Kya Hai?
Aamtaur par agar aapko apni website ya app se users ko SMS (jaise Login OTP ya Order alert) bhejna ho, toh aapko **Twilio, Fast2SMS, MSG91** jaisi paid services leni padti hain, jisme:
- Har SMS ka 20 paisa se ₹1.50 tak lagta hai.
- Monthly recharge aur DLT registration ka jhanjhat hota hai.

**OpenSMS ke sath:**
- Aap apne phone ke **Jio / Airtel / Vi SIM** ke **Unlimited SMS pack** (roz ke 100 free SMS) ka use karke **bina ₹1 kharch kiye** direct API ke zariye SMS bhej sakte hain!
- Saara data aapke paas rehta hai, koi third-party company aapka data ya phone numbers nahi dekh sakti.

---

## 💡 2. Iske Top 5 Fayde (Use Cases)

1. **Free Login OTP:** Apni website ya app me user verification ke liye OTP bhejein.
2. **Order & Billing Alerts:** Dukaan ya billing software se customer ke phone par receipt ya payment confirmation message bhejein.
3. **Server Down Alert:** Agar aapka computer ya database band ho jaye, toh turant aapke phone par SMS aa jayega.
4. **Home Automation / CCTV Alert:** Ghar par motion detect hone par bina internet ke bhi SMS alert trigger karein.
5. **No Monthly Charges:** Poori tarah free aur lifetime open-source.

---

## 🏗️ 3. Yeh Kaam Kaise Karta Hai? (Architecture)

```
[ Aapka Computer / Website / Python Script ]
                     │
                     │  (Wi-Fi ke zariye HTTP POST Request)
                     ▼
       [ OpenSMS Server (Port 9292) ]
                     │
                     │  (Phone ka SMS Manager)
                     ▼
           [ Phone ka SIM Card ]
                     │
                     │  (Cellular Network)
                     ▼
       [ Customer / User ka Mobile Phone ]
```

---

## 🚀 4. Step-by-Step Setup Guide

Asli mobile number par SMS deliver karne ke liye setup do hisso me hota hai:
1. **Phone Setup** (Jahan SIM laga hai jo SMS bhejega)
2. **Computer / Web Panel Setup** (Jahan se aap message trigger karenge)

---

### 📱 Part A: Android Phone Setup (Real SMS Bhejne Ke Liye)

Kyunki computer me SIM card nahi hota, asli SMS bhejne ke liye phone me OpenSMS chalana zaroori hai.

#### Step 1: APK Download & Install Karein
1. Apne Android phone me yeh link open karke APK download karein:  
   👉 [Download OpenSMS APK (Official Release)](https://github.com/ajzcoding1/Open-SMS/releases)
2. File download hone ke baad install karein. *(Agar Google Play Protect warning aaye, toh "Install Anyway" par tap karein)*.

#### Step 2: SMS Permission Allow Karein (Zaroori Step)
Android security ke kaaran SMS permission block kar deta hai, isliye:
1. Phone ki **Settings** > **Apps** > **OpenSMS** me jayein.
2. Top-right corner me **3 dots (⋮)** par click karein aur **"Allow restricted settings"** select karein.
3. Fir **Permissions** me jakar **SMS: Allow** kar dein.

#### Step 3: Phone me Server Start Karein
1. OpenSMS app open karein.
2. Pehli baar kholne par API Key generate hogi, use copy karke rakh lein.
3. Screen par **"Start Server"** button dabayein.
4. Screen par aapke phone ka **Local Wi-Fi IP** dikhega (Jaise: `192.168.1.15:9292`).

---

### 💻 Part B: Computer / PC Se SMS Bhejna (Web Panel)

Phone ka server on hone ke baad aap PC se bina kisi extra software ke SMS bhej sakte hain:

1. Dhyan rahe PC aur Android Phone dono **same Wi-Fi** se connected hon (ya phone ka hotspot PC se connect ho).
2. PC ke browser (Chrome / Edge) me Phone ki screen par dikh raha IP address kholein:  
   👉 **http://<PHONE-IP>:9292/** *(Jaise: `http://192.168.1.15:9292/`)*
3. Yahan aapko ek **Visual Dashboard** milega:
   - 🟢 **Server Status:** Live dikhega
   - 🔑 **API Key:** Automatically pre-filled milegi
   - 📱 **Recipient Number:** Mobile number daalein (Country code ke sath, jaise `+919876543210`)
   - 💬 **SMS Message:** Apna message likhein
   - 🚀 **"Send SMS Now"** dabate hi aapke Phone ke SIM se Real SMS chala jayega!

---

## 🔑 5. API Key Kahan Milegi aur Kaise Use Karein?

Aapki secret API key aapke folder me **`API_KEY.txt`** file ke andar saved hai:

```text
osms_E14_UaD5YnjEcInrDgrxKenFxxC0HePudrGtAB3gHu4
```

### Authorization Header Format:
Jab bhi aap code se call karein, header me yeh bhejna hota hai:
```http
Authorization: Bearer osms_E14_UaD5YnjEcInrDgrxKenFxxC0HePudrGtAB3gHu4
```

---

## 💻 6. Code Examples (SMS Kaise Bhejein)

### A. Python Code Se:
```python
import requests

# Phone ka IP ya local PC ka IP
url = "http://127.0.0.1:9292/sms/"

headers = {
    "Authorization": "Bearer osms_E14_UaD5YnjEcInrDgrxKenFxxC0HePudrGtAB3gHu4",
    "Content-Type": "application/json"
}

payload = {
    "number": "+919876543210",
    "text": "Hello! Aapka OTP hai: 4829"
}

response = requests.post(url, json=payload, headers=headers)
print("Response:", response.json())
# Output: {'stat': True}
```

### B. cURL / Terminal Se:
```bash
curl -i -X POST 'http://127.0.0.1:9292/sms/' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer osms_E14_UaD5YnjEcInrDgrxKenFxxC0HePudrGtAB3gHu4' \
  -d '{"number": "+919876543210", "text": "Testing OpenSMS gateway"}'
```

### C. JavaScript (Node.js / Frontend Fetch) Se:
```javascript
fetch("http://127.0.0.1:9292/sms/", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "Authorization": "Bearer osms_E14_UaD5YnjEcInrDgrxKenFxxC0HePudrGtAB3gHu4"
  },
  body: JSON.stringify({
    number: "+919876543210",
    text: "Order #1042 successfully confirmed!"
  })
})
.then(res => res.json())
.then(data => console.log(data));
```

---

## 🛠️ 7. Common Problems aur Solutions (Troubleshooting)

### Q1: Computer se Phone par request nahi ja rahi?
* **Solution:** Make sure karein ki aapka **Computer aur Android Phone dono same Wi-Fi network** par connected hon. Computer me phone ka Wi-Fi IP daalein (jaise `http://192.168.x.x:9292/`).

### Q2: Phone se SMS send nahi ho raha?
* **Solution:** 
  1. Phone ki Settings me jakar check karein ki **SMS Permission Allow** hai ya nahi.
  2. SIM card me SMS balance ya active plan hona zaroori hai.
  3. Phone ka "Battery Optimization" OpenSMS app ke liye OFF kar dein taaki background me server band na ho.

### Q3: Server dubara kaise start karein?
* **Solution:** Phone me app kholein aur "Start Server" par click karein.
