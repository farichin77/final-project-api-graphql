# ⚡ Quick Setup - Slack Notifications

## TL;DR (30 detik setup)

### 1. Dapatkan Slack Webhook URL
- Buka: https://api.slack.com/apps
- Create New App → "Test Automation"
- Incoming Webhooks → Add to Workspace
- Copy webhook URL

### 2. Set GitHub Secret
- Repository Settings → Secrets and variables → Actions
- New Secret: 
  - Name: `SLACK_WEBHOOK_URL`
  - Value: Paste webhook URL
- Save

### 3. Verify Workflow
- Push code ke GitHub
- Tunggu workflow selesai
- Cek Slack channel untuk notifikasi

---

## ❓ Notifikasi Tidak Terkirim?

**Cek checklist:** Buka `DEBUG_CHECKLIST.md`

**Quick test webhook:**
```bash
# Linux/Mac:
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"Test"}' \
  YOUR_WEBHOOK_URL

# Windows PowerShell:
$webhook = "YOUR_WEBHOOK_URL"
$body = @{ text = "Test" } | ConvertTo-Json
Invoke-WebRequest -Uri $webhook -Method Post -Body $body -ContentType 'application/json'
```

**Response 200 = Webhook valid ✅**

---

## 📁 Files yang ditambah:

- `.github/SLACK_SETUP.md` - Detailed setup guide
- `DEBUG_CHECKLIST.md` - Debugging steps
- `.github/workflows/test.yml` - Updated dengan debug output
- `scripts/test-slack-webhook.sh` - Script untuk test webhook

---

**Status:** ✅ Ready to use!
