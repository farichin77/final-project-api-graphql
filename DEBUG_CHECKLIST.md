# 🔍 Debug Checklist - Slack Notification tidak Terkirim

## 1️⃣ Verifikasi GitHub Secrets Configuration

Pergi ke: **Settings → Secrets and variables → Actions**

Pastikan ada secret bernama: `SLACK_WEBHOOK_URL`

```
❓ Apakah SLACK_WEBHOOK_URL sudah di-set?
  ☐ YA - Lanjut ke step 2
  ☐ TIDAK - Buat secret baru:
    • Name: SLACK_WEBHOOK_URL
    • Value: Paste webhook URL dari Slack
```

## 2️⃣ Cek Webhook URL Valid

**Opsi A: Manual Test (Linux/Mac)**
```bash
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"Test message"}' \
  YOUR_WEBHOOK_URL
```

**Opsi B: Gunakan Script**
```bash
./scripts/test-slack-webhook.sh YOUR_WEBHOOK_URL
```

## 3️⃣ Cek GitHub Actions Logs

1. Buka repository → **Actions**
2. Klik workflow yang terakhir
3. Buka step **"Run tests"**
4. Scroll ke bawah, cari output seperti:

```
🔍 PRE-TEST CONFIGURATION CHECK
════════════════════════════════════════════════
📌 Environment Variables:
  • SLACK_WEBHOOK_URL: ✅ CONFIGURED
  • SLACK_ENABLE_NOTIFICATIONS: true
```

```
❓ Apakah SLACK_WEBHOOK_URL terlihat CONFIGURED?
  ☐ YES - Lanjut ke step 4
  ☐ NO - Kembali ke step 1 (secret tidak tersimpan)
```

## 4️⃣ Cek Slack Notification Output

Di log yang sama, cari output seperti:

```
📋 Slack Notification Check:
   - All suites completed: true
   - Notification sent: false
   - Completed suites: [Login Tests, Create and Update Tests, Get Data Verification Tests, Delete Tests]
```

```
📤 Sending to Slack...
   Webhook URL length: 127 chars
   Request prepared, sending...
   Response status: 200
   ✅ Message sent successfully!
```

```
❓ Apakah response status 200?
  ☐ YES - Notifikasi terkirim! Cek channel Slack
  ☐ 404 - Webhook URL invalid/expired, buat yang baru
  ☐ 401 - Webhook expired, buat yang baru
  ☐ 403 - Permission denied, pastikan app punya akses
```

## 5️⃣ Common Issues & Solutions

### ❌ SLACK_WEBHOOK_URL: NOT SET

**Penyebab:** Belum di-set di GitHub Secrets

**Solusi:**
1. Settings → Secrets and variables → Actions
2. New repository secret
3. Name: `SLACK_WEBHOOK_URL`
4. Value: Webhook URL dari Slack
5. Push code baru / re-run workflow

### ❌ Response status: 404 / 401

**Penyebab:** Webhook URL invalid atau expired

**Solusi:**
1. Buka Slack workspace → api.slack.com/apps
2. Buka app "Test Automation"
3. Incoming Webhooks → regenerate/buat webhook baru
4. Copy webhook URL
5. Update SLACK_WEBHOOK_URL di GitHub Secrets

### ❌ All suites completed: false

**Penyebab:** Test belum selesai atau beberapa suite gagal

**Solusi:**
- Pastikan semua 4 test suites berhasil:
  - Login Tests
  - Create and Update Tests
  - Get Data Verification Tests
  - Delete Tests
- Cek di Extent Report untuk detail failure

### ✅ All suites completed: true tapi notif tidak terkirim

**Penyebab:** Mungkin notifikasi terkirim tapi tidak ke channel yang benar

**Solusi:**
1. Cek Slack workspace
2. Cari channel yang di-set di webhook
3. Scroll ke bawah untuk cari notifikasi terbaru
4. Atau buat webhook baru ke channel yang benar

## 6️⃣ Test End-to-End

1. **Push code ke GitHub**
   ```bash
   git add .
   git commit -m "Test Slack integration"
   git push
   ```

2. **Tunggu workflow selesai** (~2-3 menit)

3. **Cek GitHub Actions logs:**
   - Repository → Actions
   - Workflow terbaru → Run tests

4. **Cek Slack channel:**
   - Buka channel yang dikonfigurasi
   - Cari notifikasi dari "Test Automation"

## 📞 Support

Jika masih error, screenshot:
1. GitHub Actions log (terutama bagian "Run tests")
2. Error message yang muncul
3. Kirim ke developer/QA lead

---

**Last Updated:** January 20, 2026
**Status:** Active
