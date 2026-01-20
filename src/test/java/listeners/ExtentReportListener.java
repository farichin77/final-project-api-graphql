package listeners;

import org.testng.*;
import utils.ExtentManager;
import utils.TestResultsManager;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import java.util.ArrayList;
import java.util.List;

public class ExtentReportListener implements ITestListener, ISuiteListener {
    
    private long suiteStartTime;
    private List<String> failedTestNames = new ArrayList<>();
    private TestResultsManager resultsManager = TestResultsManager.getInstance();

    @Override
    public void onStart(ITestContext context) {
        ExtentManager.getInstance();
        suiteStartTime = System.currentTimeMillis();
        failedTestNames.clear();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
        
        // Calculate execution time
        long executionTime = System.currentTimeMillis() - suiteStartTime;
        
        // Collect results
        String testName = context.getName();
        int total = context.getAllTestMethods().length;
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        
        resultsManager.addSuiteResults(testName, total, passed, failed, skipped);
        
        // Add failed test names
        for (String failedTest : failedTestNames) {
            resultsManager.addFailedTest(failedTest);
        }
        
        failedTestNames.clear();
    }
    
    @Override
    public void onStart(ISuite suite) {
        // Initialize on first suite start
        if (resultsManager.getCompletedSuites().isEmpty()) {
            resultsManager.setEnvironment(System.getenv("TEST_ENVIRONMENT") != null ? 
                System.getenv("TEST_ENVIRONMENT") : "CI/CD");
            resultsManager.setTriggeredBy(System.getenv("GITHUB_ACTOR") != null ? 
                System.getenv("GITHUB_ACTOR") : "Manual");
            
            String reportUrl = System.getenv("REPORT_URL");
            if (reportUrl != null && !reportUrl.isEmpty()) {
                resultsManager.setReportUrl(reportUrl);
            }
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        // Count all test results from this suite
        int total = 0;
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        
        java.util.Map<String, org.testng.ISuiteResult> results = suite.getResults();
        for (org.testng.ISuiteResult result : results.values()) {
            org.testng.ITestContext context = result.getTestContext();
            passed += context.getPassedTests().size();
            failed += context.getFailedTests().size();
            skipped += context.getSkippedTests().size();
        }
        
        total = passed + failed + skipped;
        
        // Mark suite as completed and try to send notification
        resultsManager.markSuiteCompleted(suite.getName());
        resultsManager.sendSlackNotification();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription() != null ? 
                           result.getMethod().getDescription() : 
                           "Test: " + testName;
        
        // Extract category from package name
        String packageName = result.getTestClass().getName();
        String category = extractCategoryFromPackage(packageName);
        
        // Create test with category and visual styling
        ExtentManager.createTest(testName, description, category);
        
        // Add test metadata with professional formatting
        String testInfo = "<details><summary><b>Test Information</b></summary>" +
                         "<table border='1' style='border-collapse: collapse; width: 100%; color: #e0e0e0; border-color: #444;'>" +
                         "<tr><td style='padding: 8px; background-color: #333;'><b>Test Name</b></td><td style='padding: 8px;'>" + testName + "</td></tr>" +
                         "<tr><td style='padding: 8px; background-color: #333;'><b>Category</b></td><td style='padding: 8px;'>" + category + "</td></tr>" +
                         "<tr><td style='padding: 8px; background-color: #333;'><b>Test Class</b></td><td style='padding: 8px;'>" + result.getTestClass().getName() + "</td></tr>" +
                         "<tr><td style='padding: 8px; background-color: #333;'><b>Priority</b></td><td style='padding: 8px;'>" + getPriority(result) + "</td></tr>" +
                         "</table></details>";
        
        ExtentManager.getTest().info(testInfo);
        
        // Add test parameters to report
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            StringBuilder paramInfo = new StringBuilder("Test Parameters:<br>");
            for (int i = 0; i < parameters.length; i++) {
                paramInfo.append("Parameter ").append(i + 1).append(": ").append(parameters[i]).append("<br>");
            }
            ExtentManager.getTest().info(paramInfo.toString());
            
            // Extract scenario from test data parameter
            String scenario = extractScenarioFromParameter(parameters[0]);
            if (scenario != null && !scenario.isEmpty()) {
                String scenarioInfo = "<div style='background-color: #1e3a5f; color: #ffffff; padding: 8px; border-radius: 3px; border-left: 4px solid #0066cc; margin-top: 10px;'>" +
                                     "<b>Test Scenario:</b> " + scenario +
                                     "</div>";
                ExtentManager.getTest().info(scenarioInfo);
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String packageName = result.getTestClass().getName();
        String category = extractCategoryFromPackage(packageName);
        
        // Add success indicator
        ExtentManager.getTest().log(Status.PASS, "<b>Test Passed Successfully</b>");
        ExtentManager.getTest().log(Status.INFO, "Test completed successfully in " + category + " module");
        
        // Add execution time with professional formatting
        long duration = result.getEndMillis() - result.getStartMillis();
        String timeInfo = String.format("<b>Execution Time:</b> %d ms (%.2f seconds)", duration, duration / 1000.0);
        ExtentManager.getTest().log(Status.INFO, timeInfo);
        
        // Add success summary with scenario
        String scenario = extractScenarioFromTestResult(result);
        String successSummary = "<div style='background-color: #1a3a1a; color: #ffffff; padding: 10px; border-radius: 3px; border-left: 4px solid #28a745;'>" +
                               "<b>Success Summary:</b><br>" +
                               "Test: " + testName + "<br>" +
                               "Category: " + category + "<br>" +
                               (scenario != null ? "Scenario: " + scenario + "<br>" : "") +
                               "Status: <span style='color: #28a745; font-weight: bold;'>PASSED</span>" +
                               "</div>";
        ExtentManager.getTest().info(successSummary);
        
        System.out.println("✓ PASS: " + testName + " [" + category + "]");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String packageName = result.getTestClass().getName();
        String category = extractCategoryFromPackage(packageName);
        String exceptionMessage = result.getThrowable() != null ? 
                                result.getThrowable().getMessage() : "Unknown error";
        
        // Track failed test for Slack notification
        failedTestNames.add(testName + " (" + category + ")");
        
        // Add failure indicator
        ExtentManager.getTest().log(Status.FAIL, "<b>Test Failed</b>");
        ExtentManager.getTest().log(Status.FAIL, "Test failed in " + category + " module");
        ExtentManager.getTest().log(Status.FAIL, "<b>Failure Reason:</b> " + exceptionMessage);
        
        // Add execution time with professional formatting
        long duration = result.getEndMillis() - result.getStartMillis();
        String timeInfo = String.format("<b>Execution Time:</b> %d ms (%.2f seconds)", duration, duration / 1000.0);
        ExtentManager.getTest().log(Status.INFO, timeInfo);
        
        // Add failure summary with scenario and professional styling
        String scenario = extractScenarioFromTestResult(result);
        String failureSummary = "<div style='background-color: #4a1a1a; color: #ffffff; padding: 10px; border-radius: 3px; border-left: 4px solid #dc3545;'>" +
                               "<b>Failure Summary:</b><br>" +
                               "Test: " + testName + "<br>" +
                               "Category: " + category + "<br>" +
                               (scenario != null ? "Scenario: " + scenario + "<br>" : "") +
                               "Status: <span style='color: #dc3545; font-weight: bold;'>FAILED</span><br>" +
                               "Error: <code>" + exceptionMessage + "</code>" +
                               "</div>";
        ExtentManager.getTest().fail(failureSummary);
        
        // Add test class and method info with professional formatting
        ExtentManager.getTest().fail(MarkupHelper.createLabel("Test Case Failed: " + testName, ExtentColor.RED));
        ExtentManager.getTest().fail("Class: " + result.getTestClass().getName());
        ExtentManager.getTest().fail("Method: " + result.getMethod().getMethodName());
        ExtentManager.getTest().fail("Module: " + category);
        
        // Add full stack trace
        if (result.getThrowable() != null) {
            ExtentManager.getTest().fail(result.getThrowable());
            
            // Add custom error analysis for common issues
            String errorAnalysis = analyzeError(result.getThrowable().getMessage());
            if (errorAnalysis != null) {
                String analysisBox = "<div style='background-color: #3d2b1f; color: #ffffff; padding: 10px; border-radius: 3px; border-left: 4px solid #ff9800;'>" +
                                     "<b>Error Analysis:</b><br>" + errorAnalysis +
                                     "</div>";
                ExtentManager.getTest().info(analysisBox);
            }
        }
        
        System.out.println("✗ FAIL: " + testName + " [" + category + "] - " + exceptionMessage);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String packageName = result.getTestClass().getName();
        String category = extractCategoryFromPackage(packageName);
        String skipReason = result.getThrowable() != null ? 
                           result.getThrowable().getMessage() : "Unknown reason";
        
        // Add skip indicator
        ExtentManager.getTest().log(Status.SKIP, "<b>Test Skipped</b>");
        ExtentManager.getTest().log(Status.SKIP, "Test skipped in " + category + " module");
        ExtentManager.getTest().log(Status.SKIP, "<b>Skip Reason:</b> " + skipReason);
        
        // Add skip summary with scenario and professional styling
        String scenario = extractScenarioFromTestResult(result);
        String skipSummary = "<div style='background-color: #3d2b1f; color: #ffffff; padding: 10px; border-radius: 3px; border-left: 4px solid #ff9800;'>" +
                             "<b>Skip Summary:</b><br>" +
                             "Test: " + testName + "<br>" +
                             "Category: " + category + "<br>" +
                             (scenario != null ? "Scenario: " + scenario + "<br>" : "") +
                             "Status: <span style='color: #ff9800; font-weight: bold;'>SKIPPED</span><br>" +
                             "Reason: " + skipReason +
                             "</div>";
        ExtentManager.getTest().skip(skipSummary);
        ExtentManager.getTest().skip(MarkupHelper.createLabel("Test Skipped: " + testName, ExtentColor.YELLOW));
        
        System.out.println("- SKIP: " + testName + " [" + category + "] - " + skipReason);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String packageName = result.getTestClass().getName();
        String category = extractCategoryFromPackage(packageName);
        
        ExtentManager.getTest().log(Status.WARNING, "<b>Test Partially Failed</b>");
        ExtentManager.getTest().log(Status.WARNING, "Test partially failed in " + category + " module");
        ExtentManager.getTest().warning(MarkupHelper.createLabel("Test Partially Failed: " + testName, ExtentColor.ORANGE));
        
        // Add partial failure summary with scenario
        String scenario = extractScenarioFromTestResult(result);
        String partialSummary = "<div style='background-color: #3d2b1f; color: #ffffff; padding: 10px; border-radius: 3px; border-left: 4px solid #ff9800;'>" +
                               "<b>Partial Failure Summary:</b><br>" +
                               "Test: " + testName + "<br>" +
                               "Category: " + category + "<br>" +
                               (scenario != null ? "Scenario: " + scenario + "<br>" : "") +
                               "Status: <span style='color: #ff9800; font-weight: bold;'>PARTIALLY FAILED</span>" +
                               "</div>";
        ExtentManager.getTest().warning(partialSummary);
        
        System.out.println("! PARTIAL: " + testName + " [" + category + "]");
    }
    
    /**
     * Extract category name from package name
     * Simplified to show only 2 main modules:
     * - Employee Module (includes employee and division)
     * - Training Module (includes training, chapter, and content)
     */
    private String extractCategoryFromPackage(String packageName) {
        if (packageName == null) return "Unknown";
        
        // Extract category from package structure
        String[] parts = packageName.split("\\.");
        
        // Check for module-based categorization
        for (String part : parts) {
            String lowerPart = part.toLowerCase();
            
            // Employee Module (includes division and employee)
            if (lowerPart.equals("employeemodule")) {
                return "Employee Module";
            }
            
            // Training Module (includes training, chapter, and content)
            if (lowerPart.equals("trainingmodule")) {
                return "Training Module";
            }
            
            // Authentication (standalone)
            if (lowerPart.equals("auth")) {
                return "Authentication";
            }
        }
        
        return "General";
    }
    
    /**
     * Get icon for category
     */
    private String getCategoryIcon(String category) {
        if (category == null) return "[TEST]";
        
        String lowerCategory = category.toLowerCase();
        
        if (lowerCategory.contains("authentication")) {
            return "[AUTH]";
        } else if (lowerCategory.contains("employee module")) {
            if (lowerCategory.contains("division")) {
                return "[EMPLOYEE-DIV]";
            } else if (lowerCategory.contains("employee")) {
                return "[EMPLOYEE-EMP]";
            }
            return "[EMPLOYEE]";
        } else if (lowerCategory.contains("training module")) {
            if (lowerCategory.contains("chapter")) {
                return "[TRAINING-CHAP]";
            } else if (lowerCategory.contains("content")) {
                return "[TRAINING-CONT]";
            } else if (lowerCategory.contains("training")) {
                return "[TRAINING-TRN]";
            }
            return "[TRAINING]";
        }
        
        return "[TEST]";
    }
    
    /**
     * Get test priority
     */
    private String getPriority(ITestResult result) {
        // Try to get priority from test method annotation
        try {
            org.testng.annotations.Test testAnnotation = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(org.testng.annotations.Test.class);
            if (testAnnotation != null) {
                int priority = testAnnotation.priority();
                if (priority == 0) return "Normal";
                else if (priority < 0) return "High";
                else return "Low";
            }
        } catch (Exception e) {
            // Ignore
        }
        return "Normal";
    }
    
    /**
     * Extract scenario from test data parameter
     */
    private String extractScenarioFromParameter(Object parameter) {
        if (parameter == null) return null;
        
        try {
            // Use reflection to get scenario field from test data objects
            String paramStr = parameter.toString();
            
            // Try to extract scenario using reflection first
            try {
                java.lang.reflect.Field scenarioField = parameter.getClass().getDeclaredField("scenario");
                scenarioField.setAccessible(true);
                Object scenarioValue = scenarioField.get(parameter);
                if (scenarioValue != null) {
                    return scenarioValue.toString();
                }
            } catch (Exception e) {
                // Reflection failed, try string parsing
            }
            
            // Fallback to string parsing
            if (paramStr.contains("scenario=")) {
                int scenarioStart = paramStr.indexOf("scenario=") + 9;
                int scenarioEnd = paramStr.indexOf(",", scenarioStart);
                if (scenarioEnd == -1) {
                    scenarioEnd = paramStr.indexOf("}", scenarioStart);
                }
                if (scenarioEnd > scenarioStart) {
                    return paramStr.substring(scenarioStart, scenarioEnd).trim();
                }
            }
        } catch (Exception e) {
            // Ignore all errors
        }
        return null;
    }
    
    /**
     * Extract scenario from test result parameters
     */
    private String extractScenarioFromTestResult(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            return extractScenarioFromParameter(parameters[0]);
        }
        return null;
    }
    
    private String analyzeError(String errorMessage) {
        if (errorMessage == null) return null;
        
        String lowerError = errorMessage.toLowerCase();
        
        if (lowerError.contains("content not found")) {
            return "This error occurs when trying to access or delete content that doesn't exist in the database.<br>" +
                   "Possible solutions:<br>" +
                   "1. Verify the content ID exists in the database<br>" +
                   "2. Check if the content was already deleted<br>" +
                   "3. Ensure proper test data setup";
        } else if (lowerError.contains("authentication") || lowerError.contains("unauthorized")) {
            return "Authentication error detected.<br>" +
                   "Possible solutions:<br>" +
                   "1. Check login credentials<br>" +
                   "2. Verify session is valid<br>" +
                   "3. Check API endpoint permissions";
        } else if (lowerError.contains("timeout") || lowerError.contains("connection")) {
            return "Network/Connection error detected.<br>" +
                   "Possible solutions:<br>" +
                   "1. Check network connectivity<br>" +
                   "2. Verify API server is running<br>" +
                   "3. Increase timeout values";
        } else if (lowerError.contains("assertion")) {
            return "Test assertion failed.<br>" +
                   "This indicates the actual result doesn't match the expected result.<br>" +
                   "Review the test logic and expected values.";
        }
        
        return null;
    }
}
