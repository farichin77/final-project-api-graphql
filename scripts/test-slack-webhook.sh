#!/bin/bash

# Script untuk test Slack Webhook tanpa menjalankan test suite

echo "════════════════════════════════════════════════════════════════"
echo "🧪 SLACK WEBHOOK TEST SCRIPT"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Check if webhook URL provided
if [ -z "$1" ]; then
    echo "❌ Error: Slack Webhook URL tidak diberikan"
    echo ""
    echo "Usage:"
    echo "  ./test-slack-webhook.sh YOUR_WEBHOOK_URL"
    echo ""
    echo "Contoh:"
    echo "  ./test-slack-webhook.sh https://hooks.slack.com/services/T.../B.../XXXX"
    echo ""
    exit 1
fi

WEBHOOK_URL=$1

echo "📍 Testing Slack Webhook..."
echo "   URL: ${WEBHOOK_URL:0:50}..."
echo ""

# Test dengan simple message
echo "▶️  Sending test message..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H 'Content-type: application/json' \
  --data '{
    "blocks": [
      {
        "type": "header",
        "text": {
          "type": "plain_text",
          "text": "✅ Test Automation - PASSED",
          "emoji": true
        }
      },
      {
        "type": "divider"
      },
      {
        "type": "section",
        "text": {
          "type": "mrkdwn",
          "text": "*📊 Test Results Summary*\n─────────────────────────────\n*Total Tests:*     1\n✅ *Passed:*        1 (100.0%)\n❌ *Failed:*        0\n⏭️ *Skipped:*       0\n─────────────────────────────"
        }
      },
      {
        "type": "section",
        "fields": [
          {
            "type": "mrkdwn",
            "text": "*⏱️ Execution Time*\n0m 5s"
          },
          {
            "type": "mrkdwn",
            "text": "*🌍 Environment*\nManual Test"
          }
        ]
      }
    ],
    "attachments": [
      {
        "color": "#2eb886",
        "fallback": "Test Results: PASSED"
      }
    ]
  }' \
  "$WEBHOOK_URL")

echo "   Response Status: $RESPONSE"
echo ""

if [ "$RESPONSE" = "200" ]; then
    echo "✅ SUCCESS! Webhook URL is valid and working"
    echo "   Check your Slack channel for the test message"
else
    echo "❌ FAILED! Response code: $RESPONSE"
    echo ""
    echo "Possible causes:"
    if [ "$RESPONSE" = "404" ]; then
        echo "  • 404: Webhook URL is invalid or channel was deleted"
    elif [ "$RESPONSE" = "401" ]; then
        echo "  • 401: Webhook URL is unauthorized (might be expired)"
    elif [ "$RESPONSE" = "403" ]; then
        echo "  • 403: App doesn't have permission to post"
    fi
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
