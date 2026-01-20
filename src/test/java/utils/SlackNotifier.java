package utils;

import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SlackNotifier {
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final String webhookUrl;
    
    public SlackNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.client = new OkHttpClient();
    }
    
    /**
     * Send test results summary to Slack
     */
    public void sendTestResults(TestResultsSummary summary) {
        try {
            String payload = buildSlackPayload(summary);
            sendMessage(payload);
            System.out.println("✓ Slack notification sent successfully");
        } catch (Exception e) {
            System.err.println("✗ Failed to send Slack notification: " + e.getMessage());
        }
    }
    
    /**
     * Build Slack message payload with rich formatting
     */
    private String buildSlackPayload(TestResultsSummary summary) {
        JsonObject payload = new JsonObject();
        
        // Determine status emoji and color
        String statusEmoji = summary.failed == 0 ? "🎉" : "⚠️";
        String statusText = summary.failed == 0 ? "PASSED" : "FAILED";
        String color = summary.failed == 0 ? "#28a745" : "#dc3545";
        
        // Calculate pass percentage
        double passPercentage = summary.total > 0 ? 
            (summary.passed * 100.0 / summary.total) : 0;
        
        // Build blocks for rich formatting
        JsonArray blocks = new JsonArray();
        
        // Header block
        JsonObject headerBlock = new JsonObject();
        headerBlock.addProperty("type", "header");
        JsonObject headerText = new JsonObject();
        headerText.addProperty("type", "plain_text");
        headerText.addProperty("text", statusEmoji + " Test Automation - " + statusText);
        headerText.addProperty("emoji", true);
        headerBlock.add("text", headerText);
        blocks.add(headerBlock);
        
        // Divider
        JsonObject divider1 = new JsonObject();
        divider1.addProperty("type", "divider");
        blocks.add(divider1);
        
        // Test Results Section
        JsonObject resultsSection = new JsonObject();
        resultsSection.addProperty("type", "section");
        JsonObject resultsText = new JsonObject();
        resultsText.addProperty("type", "mrkdwn");
        
        StringBuilder resultsMarkdown = new StringBuilder();
        resultsMarkdown.append("*📊 Test Results Summary*\n");
        resultsMarkdown.append("```\n");
        resultsMarkdown.append(String.format("✓ Total Tests:  %d\n", summary.total));
        resultsMarkdown.append(String.format("✓ Passed:       %d (%.1f%%)\n", summary.passed, passPercentage));
        resultsMarkdown.append(String.format("✗ Failed:       %d\n", summary.failed));
        resultsMarkdown.append(String.format("⊘ Skipped:      %d\n", summary.skipped));
        resultsMarkdown.append("```");
        
        resultsText.addProperty("text", resultsMarkdown.toString());
        resultsSection.add("text", resultsText);
        blocks.add(resultsSection);
        
        // Failed tests details (if any)
        if (summary.failed > 0 && summary.failedTests != null && !summary.failedTests.isEmpty()) {
            JsonObject failedSection = new JsonObject();
            failedSection.addProperty("type", "section");
            JsonObject failedText = new JsonObject();
            failedText.addProperty("type", "mrkdwn");
            
            StringBuilder failedMarkdown = new StringBuilder();
            failedMarkdown.append("*❌ Failed Tests:*\n");
            int displayCount = Math.min(summary.failedTests.size(), 5);
            for (int i = 0; i < displayCount; i++) {
                failedMarkdown.append("• ").append(summary.failedTests.get(i)).append("\n");
            }
            if (summary.failedTests.size() > 5) {
                failedMarkdown.append(String.format("_...and %d more_\n", summary.failedTests.size() - 5));
            }
            
            failedText.addProperty("text", failedMarkdown.toString());
            failedSection.add("text", failedText);
            blocks.add(failedSection);
        }
        
        // Divider
        JsonObject divider2 = new JsonObject();
        divider2.addProperty("type", "divider");
        blocks.add(divider2);
        
        // Metadata Section
        JsonObject metadataSection = new JsonObject();
        metadataSection.addProperty("type", "section");
        
        JsonArray fields = new JsonArray();
        
        // Execution Time
        JsonObject timeField = new JsonObject();
        timeField.addProperty("type", "mrkdwn");
        timeField.addProperty("text", "*⏱️ Execution Time:*\n" + summary.executionTime);
        fields.add(timeField);
        
        // Environment
        JsonObject envField = new JsonObject();
        envField.addProperty("type", "mrkdwn");
        envField.addProperty("text", "*🌍 Environment:*\n" + summary.environment);
        fields.add(envField);
        
        // Triggered By
        JsonObject userField = new JsonObject();
        userField.addProperty("type", "mrkdwn");
        userField.addProperty("text", "*👤 Triggered by:*\n" + summary.triggeredBy);
        fields.add(userField);
        
        // Timestamp
        JsonObject timestampField = new JsonObject();
        timestampField.addProperty("type", "mrkdwn");
        String timestamp = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss").format(new Date());
        timestampField.addProperty("text", "*📅 Timestamp:*\n" + timestamp);
        fields.add(timestampField);
        
        metadataSection.add("fields", fields);
        blocks.add(metadataSection);
        
        // Report Link (if available)
        if (summary.reportUrl != null && !summary.reportUrl.isEmpty()) {
            JsonObject linkSection = new JsonObject();
            linkSection.addProperty("type", "section");
            JsonObject linkText = new JsonObject();
            linkText.addProperty("type", "mrkdwn");
            linkText.addProperty("text", "*🔗 <" + summary.reportUrl + "|View Detailed Report>*");
            linkSection.add("text", linkText);
            blocks.add(linkSection);
        }
        
        payload.add("blocks", blocks);
        
        // Add color attachment for visual impact
        JsonArray attachments = new JsonArray();
        JsonObject attachment = new JsonObject();
        attachment.addProperty("color", color);
        attachment.addProperty("fallback", "Test Results: " + statusText);
        attachments.add(attachment);
        payload.add("attachments", attachments);
        
        return payload.toString();
    }
    
    /**
     * Send message to Slack webhook
     */
    private void sendMessage(String payload) throws IOException {
        RequestBody body = RequestBody.create(payload, JSON);
        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Slack API returned error: " + response.code() + " - " + response.message());
            }
        }
    }
    
    /**
     * Test Results Summary Data Class
     */
    public static class TestResultsSummary {
        public int total;
        public int passed;
        public int failed;
        public int skipped;
        public String executionTime;
        public String environment;
        public String triggeredBy;
        public String reportUrl;
        public java.util.List<String> failedTests;
        
        public TestResultsSummary(int total, int passed, int failed, int skipped) {
            this.total = total;
            this.passed = passed;
            this.failed = failed;
            this.skipped = skipped;
            this.executionTime = "N/A";
            this.environment = "Test";
            this.triggeredBy = "Manual";
            this.reportUrl = "";
            this.failedTests = new java.util.ArrayList<>();
        }
        
        public void setExecutionTime(long millis) {
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            this.executionTime = String.format("%dm %ds", minutes, seconds);
        }
        
        public void addFailedTest(String testName) {
            this.failedTests.add(testName);
        }
    }
}
