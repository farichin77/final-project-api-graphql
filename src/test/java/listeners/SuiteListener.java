package listeners;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import utils.GlobalTestResultsTracker;
import utils.SlackNotifier;
import java.util.Arrays;
import java.util.List;

/**
 * Listener for test suite lifecycle events
 * Detects when all test suites complete and sends aggregated Slack notification
 */
public class SuiteListener implements ISuiteListener {
    
    // List of all expected test suites
    private static final List<String> EXPECTED_SUITES = Arrays.asList(
        "Login Tests",
        "Create and Update Tests",
        "Get Data Verification Tests",
        "Delete Tests"
    );
    
    @Override
    public void onStart(ISuite suite) {
        String suiteName = suite.getName();
        System.out.println("▶️ Test Suite Started: " + suiteName);
        
        // Initialize global tracker
        GlobalTestResultsTracker tracker = GlobalTestResultsTracker.getInstance();
        if (!tracker.getCompletedSuites().isEmpty() && tracker.getCompletedSuites().size() == 1) {
            // First suite, initialize
            tracker.setEnvironment(System.getenv("TEST_ENVIRONMENT") != null ? 
                System.getenv("TEST_ENVIRONMENT") : "CI/CD");
            tracker.setTriggeredBy(System.getenv("GITHUB_ACTOR") != null ? 
                System.getenv("GITHUB_ACTOR") : "Manual");
            
            String reportUrl = System.getenv("REPORT_URL");
            if (reportUrl != null && !reportUrl.isEmpty()) {
                tracker.setReportUrl(reportUrl);
            }
        }
    }
    
    @Override
    public void onFinish(ISuite suite) {
        String suiteName = suite.getName();
        System.out.println("⏹️ Test Suite Finished: " + suiteName);
        
        GlobalTestResultsTracker tracker = GlobalTestResultsTracker.getInstance();
        
        // Add results from this suite
        int total = suite.getAllInvokedMethods().size();
        int passed = suite.getInvokedMethods().stream()
            .filter(method -> method.getResults().stream().allMatch(r -> r.isSuccess()))
            .mapToInt(method -> (int) method.getResults().stream().filter(r -> r.isSuccess()).count())
            .sum();
        int failed = (int) suite.getInvokedMethods().stream()
            .filter(method -> method.getResults().stream().anyMatch(r -> !r.isSuccess() && r.getStatus() == 2))
            .count();
        int skipped = (int) suite.getInvokedMethods().stream()
            .filter(method -> method.getResults().stream().anyMatch(r -> r.getStatus() == 3))
            .count();
        
        tracker.addSuiteResults(suiteName, total, passed, failed, skipped);
        tracker.markSuiteCompleted(suiteName);
        
        // Check if all suites are completed
        if (tracker.allSuitesCompleted(EXPECTED_SUITES) && !tracker.isNotificationSent()) {
            sendFinalNotification(tracker);
            tracker.setNotificationSent(true);
        }
    }
    
    /**
     * Send final aggregated notification to Slack
     */
    private void sendFinalNotification(GlobalTestResultsTracker tracker) {
        try {
            String webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
            
            String enableNotifications = System.getenv("SLACK_ENABLE_NOTIFICATIONS");
            if (enableNotifications != null && !enableNotifications.equalsIgnoreCase("true")) {
                System.out.println("ℹ Slack notifications are disabled");
                return;
            }
            
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                System.out.println("ℹ Slack webhook URL not configured, skipping notification");
                return;
            }
            
            SlackNotifier.TestResultsSummary summary = tracker.getAggregatedSummary();
            SlackNotifier notifier = new SlackNotifier(webhookUrl);
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📤 SENDING FINAL AGGREGATED TEST RESULTS TO SLACK");
            System.out.println("=".repeat(60));
            System.out.println("Total Tests: " + summary.total);
            System.out.println("Passed: " + summary.passed);
            System.out.println("Failed: " + summary.failed);
            System.out.println("Skipped: " + summary.skipped);
            System.out.println("=".repeat(60) + "\n");
            
            notifier.sendTestResults(summary);
            
        } catch (Exception e) {
            System.err.println("Failed to send final Slack notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
