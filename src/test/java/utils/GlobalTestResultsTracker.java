package utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global tracker for aggregating test results across all test suites
 * Ensures only ONE Slack notification is sent after all tests complete
 */
public class GlobalTestResultsTracker {
    
    private static GlobalTestResultsTracker instance;
    private static final Object lock = new Object();
    
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
    
    // Flag to track if notification has been sent
    private boolean notificationSent = false;
    
    private GlobalTestResultsTracker() {
    }
    
    /**
     * Get singleton instance
     */
    public static GlobalTestResultsTracker getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new GlobalTestResultsTracker();
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
     * Add test results from a completed test suite
     */
    public synchronized void addSuiteResults(String suiteName, int total, int passed, int failed, int skipped) {
        if (completedSuites.contains(suiteName)) {
            return; // Prevent duplicate additions
        }
        
        completedSuites.add(suiteName);
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
     * Get aggregated results summary
     */
    public SlackNotifier.TestResultsSummary getAggregatedSummary() {
        SlackNotifier.TestResultsSummary summary = 
            new SlackNotifier.TestResultsSummary(
                totalTests.get(),
                passedTests.get(),
                failedTests.get(),
                skippedTests.get()
            );
        
        long executionTime = System.currentTimeMillis() - suiteStartTime;
        summary.setExecutionTime(executionTime);
        summary.environment = environment;
        summary.triggeredBy = triggeredBy;
        summary.reportUrl = reportUrl;
        
        // Add all failed tests
        for (String failedTest : failedTestNames) {
            summary.addFailedTest(failedTest);
        }
        
        return summary;
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
