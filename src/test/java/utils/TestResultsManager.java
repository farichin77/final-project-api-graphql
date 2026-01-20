package utils;

import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unified manager for tracking test results and sending Slack notifications
 * Replaces both GlobalTestResultsTracker and SlackNotifier
 */
public class TestResultsManager {
    
    private static TestResultsManager instance;
    private static final Object lock = new Object();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final Set<String> completedSuites = Collections.synchronizedSet(new HashSet<>());
    private final AtomicInteger totalTests = new AtomicInteger(0);
    private final AtomicInteger passedTests = new AtomicInteger(0);
    private final AtomicInteger failedTests = new AtomicInteger(0);
    private final AtomicInteger skippedTests = new AtomicInteger(0);
    private final List<String> failedTestNames = Collections.synchronizedList(new ArrayList<>());
    private long suiteStartTime = System.currentTimeMillis();
    
    private String environment = "Test";
    private String triggeredBy = "Manual";
    private String reportUrl = "";
    private boolean notificationSent = false;
    
    private final OkHttpClient client;
    private String webhookUrl;
    
    private TestResultsManager() {
        this.client = new OkHttpClient();
    }
    
    /**
     * Get singleton instance
     */
    public static TestResultsManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new TestResultsManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Reset tracker (useful for test runs)
     */
    public synchronized void reset() {
        completedSuites.clear();
        totalTests.set(0);
        passedTests.set(0);
        failedTests.set(0);
        skippedTests.set(0);
        failedTestNames.clear();
        suiteStartTime = System.currentTimeMillis();
        notificationSent = false;
    }
    
    /**
     * Add test results from a completed test
     */
    public synchronized void addSuiteResults(String testName, int total, int passed, int failed, int skipped) {
        totalTests.addAndGet(total);
        passedTests.addAndGet(passed);
        failedTests.addAndGet(failed);
        skippedTests.addAndGet(skipped);
    }
    
    /**
     * Add failed test name to the list
     */
    public void addFailedTest(String testName) {
        if (!failedTestNames.contains(testName)) {
            failedTestNames.add(testName);
        }
    }
    
    /**
     * Mark a suite as completed
     */
    public boolean markSuiteCompleted(String suiteName) {
        return completedSuites.add(suiteName);
    }
    
    /**
     * Check if all expected suites have completed
     */
    public synchronized boolean allSuitesCompleted(List<String> expectedSuites) {
        return completedSuites.containsAll(expectedSuites);
    }
    
    /**
     * Send aggregated test results to Slack (called after each suite completes)
     * Only sends when all expected suites are done
     */
    public void sendSlackNotification() {
        if (notificationSent) {
            return; // Already sent
        }
        
        // Check if we have results and webhook is configured
        webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return; // Webhook not configured, skip silently
        }
        
        String enableNotifications = System.getenv("SLACK_ENABLE_NOTIFICATIONS");
        if (enableNotifications != null && !enableNotifications.equalsIgnoreCase("true")) {
            return; // Notifications disabled
        }
        
        // Only send if we have collected some test results
        if (totalTests.get() == 0) {
            return; // No tests collected yet
        }
        
        try {
            String payload = buildSlackPayload();
            sendMessage(payload);
            notificationSent = true;
            System.out.println("✅ Slack notification sent");
        } catch (Exception e) {
            System.err.println("❌ Failed to send Slack notification: " + e.getMessage());
        }
    }
    
    /**
     * Build Slack message payload with rich formatting
     */
    private String buildSlackPayload() {
        JsonObject payload = new JsonObject();
        
        // Determine status
        String statusEmoji = failedTests.get() == 0 ? "✅" : "❌";
        String statusText = failedTests.get() == 0 ? "PASSED" : "FAILED";
        String color = failedTests.get() == 0 ? "#2eb886" : "#ff0000";
        
        // Build blocks for rich formatting
        JsonArray blocks = new JsonArray();
        
        // Header block with status
        JsonObject headerBlock = new JsonObject();
        headerBlock.addProperty("type", "header");
        JsonObject headerText = new JsonObject();
        headerText.addProperty("type", "plain_text");
        headerText.addProperty("text", statusEmoji + " API Test Results - " + statusText);
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
        resultsMarkdown.append("─────────────────────────────\n");
        resultsMarkdown.append(String.format("*Total Tests:*     %d\n", totalTests.get()));
        resultsMarkdown.append(String.format("✅ *Passed:*        %d\n", passedTests.get()));
        resultsMarkdown.append(String.format("❌ *Failed:*        %d\n", failedTests.get()));
        resultsMarkdown.append(String.format("⏭️ *Skipped:*       %d\n", skippedTests.get()));
        resultsMarkdown.append("─────────────────────────────");
        
        resultsText.addProperty("text", resultsMarkdown.toString());
        resultsSection.add("text", resultsText);
        blocks.add(resultsSection);
        
        // Failed tests details (if any)
        if (failedTests.get() > 0 && !failedTestNames.isEmpty()) {
            JsonObject dividerBefore = new JsonObject();
            dividerBefore.addProperty("type", "divider");
            blocks.add(dividerBefore);
            
            JsonObject failedSection = new JsonObject();
            failedSection.addProperty("type", "section");
            JsonObject failedText = new JsonObject();
            failedText.addProperty("type", "mrkdwn");
            
            StringBuilder failedMarkdown = new StringBuilder();
            failedMarkdown.append("*❌ Failed Tests:*\n");
            int displayCount = Math.min(failedTestNames.size(), 5);
            for (int i = 0; i < displayCount; i++) {
                failedMarkdown.append("  • ").append(failedTestNames.get(i)).append("\n");
            }
            if (failedTestNames.size() > 5) {
                failedMarkdown.append(String.format("  • _...and %d more_", failedTestNames.size() - 5));
            }
            
            failedText.addProperty("text", failedMarkdown.toString());
            failedSection.add("text", failedText);
            blocks.add(failedSection);
        }
        
        // Divider before metadata
        JsonObject divider2 = new JsonObject();
        divider2.addProperty("type", "divider");
        blocks.add(divider2);
        
        // Metadata Section - 2x2 grid
        JsonObject metadataSection = new JsonObject();
        metadataSection.addProperty("type", "section");
        
        JsonArray fields = new JsonArray();
        
        // Execution Time
        long executionTime = System.currentTimeMillis() - suiteStartTime;
        String timeStr = String.format("%dm %ds", executionTime / 60000, (executionTime % 60000) / 1000);
        JsonObject timeField = new JsonObject();
        timeField.addProperty("type", "mrkdwn");
        timeField.addProperty("text", "*⏱️ Execution Time*\n" + timeStr);
        fields.add(timeField);
        
        // Environment
        JsonObject envField = new JsonObject();
        envField.addProperty("type", "mrkdwn");
        envField.addProperty("text", "*🌍 Environment*\n" + environment);
        fields.add(envField);
        
        // Triggered By
        JsonObject userField = new JsonObject();
        userField.addProperty("type", "mrkdwn");
        userField.addProperty("text", "*👤 Triggered by*\n" + triggeredBy);
        fields.add(userField);
        
        // Timestamp
        JsonObject timestampField = new JsonObject();
        timestampField.addProperty("type", "mrkdwn");
        String timestamp = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss").format(new Date());
        timestampField.addProperty("text", "*📅 Timestamp*\n" + timestamp);
        fields.add(timestampField);
        
        metadataSection.add("fields", fields);
        blocks.add(metadataSection);
        
        // Report Link (if available)
        if (reportUrl != null && !reportUrl.isEmpty()) {
            JsonObject divider3 = new JsonObject();
            divider3.addProperty("type", "divider");
            blocks.add(divider3);
            
            JsonObject linkSection = new JsonObject();
            linkSection.addProperty("type", "section");
            JsonObject linkText = new JsonObject();
            linkText.addProperty("type", "mrkdwn");
            linkText.addProperty("text", "<" + reportUrl + "|📈 View Detailed Test Report>");
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
                throw new IOException("Slack API error: " + response.code() + " - " + response.message());
            }
        }
    }
    
    // Getters and setters
    public int getTotalTests() {
        return totalTests.get();
    }
    
    public int getPassedTests() {
        return passedTests.get();
    }
    
    public int getFailedTests() {
        return failedTests.get();
    }
    
    public int getSkippedTests() {
        return skippedTests.get();
    }
    
    public List<String> getFailedTestNames() {
        return new ArrayList<>(failedTestNames);
    }
    
    public Set<String> getCompletedSuites() {
        return new HashSet<>(completedSuites);
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
    
    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }
    
    public boolean isNotificationSent() {
        return notificationSent;
    }
    
    public void setNotificationSent(boolean sent) {
        this.notificationSent = sent;
    }
}
