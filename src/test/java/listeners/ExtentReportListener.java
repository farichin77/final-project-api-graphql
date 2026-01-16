package listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ExtentManager;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;

public class ExtentReportListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentManager.getInstance();
        System.out.println("=== Test Suite Started: " + context.getSuite().getName() + " ===");
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
        System.out.println("=== Test Suite Finished: " + context.getSuite().getName() + " ===");
        System.out.println("Total Tests: " + context.getAllTestMethods().length);
        System.out.println("Passed: " + context.getPassedTests().size());
        System.out.println("Failed: " + context.getFailedTests().size());
        System.out.println("Skipped: " + context.getSkippedTests().size());
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
                         "<table border='1' style='border-collapse: collapse; width: 100%;'>" +
                         "<tr><td style='padding: 8px; background-color: #f5f5f5;'><b>Test Name</b></td><td style='padding: 8px;'>" + testName + "</td></tr>" +
                         "<tr><td style='padding: 8px; background-color: #f5f5f5;'><b>Category</b></td><td style='padding: 8px;'>" + category + "</td></tr>" +
                         "<tr><td style='padding: 8px; background-color: #f5f5f5;'><b>Test Class</b></td><td style='padding: 8px;'>" + result.getTestClass().getName() + "</td></tr>" +
                         "<tr><td style='padding: 8px; background-color: #f5f5f5;'><b>Priority</b></td><td style='padding: 8px;'>" + getPriority(result) + "</td></tr>" +
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
                String scenarioInfo = "<div style='background-color: #f0f8ff; padding: 8px; border-radius: 3px; border-left: 4px solid #0066cc; margin-top: 10px;'>" +
                                     "<b>Test Scenario:</b> " + scenario +
                                     "</div>";
                ExtentManager.getTest().info(scenarioInfo);
            }
        }
        
        // Add visual separator
        ExtentManager.getTest().info("<hr style='border-top: 1px solid #cccccc;'>");
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
        String successSummary = "<div style='background-color: #f0f8ff; padding: 10px; border-radius: 3px; border-left: 4px solid #0066cc;'>" +
                               "<b>Success Summary:</b><br>" +
                               "Test: " + testName + "<br>" +
                               "Category: " + category + "<br>" +
                               (scenario != null ? "Scenario: " + scenario + "<br>" : "") +
                               "Status: <span style='color: #0066cc; font-weight: bold;'>PASSED</span>" +
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
        String failureSummary = "<div style='background-color: #fff5f5; padding: 10px; border-radius: 3px; border-left: 4px solid #dc3545;'>" +
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
                String analysisBox = "<div style='background-color: #fff9e6; padding: 10px; border-radius: 3px; border-left: 4px solid #ff9800;'>" +
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
        String skipSummary = "<div style='background-color: #fff9e6; padding: 10px; border-radius: 3px; border-left: 4px solid #ff9800;'>" +
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
        String partialSummary = "<div style='background-color: #fff9e6; padding: 10px; border-radius: 3px; border-left: 4px solid #ff9800;'>" +
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
     */
    private String extractCategoryFromPackage(String packageName) {
        if (packageName == null) return "Unknown";
        
        // Extract category from package like "tests.chapter.CreateChapterTest" -> "Chapter"
        String[] parts = packageName.split("\\.");
        for (String part : parts) {
            switch (part.toLowerCase()) {
                case "auth":
                    return "Authentication";
                case "chapter":
                    return "Chapter Management";
                case "content":
                    return "Content Management";
                case "division":
                    return "Division Management";
                case "employee":
                    return "Employee Management";
                case "training":
                    return "Training Management";
                case "userservice":
                    return "User Service";
                default:
                    break;
            }
        }
        return "General";
    }
    
    /**
     * Get icon for category
     */
    private String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "authentication":
                return "[AUTH]";
            case "chapter management":
                return "[CHAPTER]";
            case "content management":
                return "[CONTENT]";
            case "division management":
                return "[DIVISION]";
            case "employee management":
                return "[EMPLOYEE]";
            case "training management":
                return "[TRAINING]";
            case "user service":
                return "[USER]";
            default:
                return "[TEST]";
        }
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
