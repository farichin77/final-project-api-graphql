package utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportUtils {
    
    private static final String SCREENSHOT_DIR = "test-output/screenshots";
    
    static {
        // Create screenshots directory if it doesn't exist
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Add screenshot to the report
     * @param test ExtentTest instance
     * @param screenshotData Base64 encoded screenshot data
     * @param title Screenshot title
     */
    public static void addScreenshot(ExtentTest test, String screenshotData, String title) {
        try {
            if (test != null && screenshotData != null && !screenshotData.isEmpty()) {
                // Save screenshot to file
                String fileName = saveScreenshotToFile(screenshotData, title);
                
                // Add to report
                test.info(title, MediaEntityBuilder.createScreenCaptureFromPath(fileName).build());
            }
        } catch (Exception e) {
            test.warning("Failed to add screenshot: " + e.getMessage());
        }
    }
    
    /**
     * Add log with different levels
     */
    public static void logInfo(ExtentTest test, String message) {
        if (test != null) {
            test.info(message);
        }
    }
    
    public static void logPass(ExtentTest test, String message) {
        if (test != null) {
            test.pass(message);
        }
    }
    
    public static void logFail(ExtentTest test, String message) {
        if (test != null) {
            test.fail(message);
        }
    }
    
    public static void logWarning(ExtentTest test, String message) {
        if (test != null) {
            test.warning(message);
        }
    }
    
    /**
     * Add API request/response details to report
     */
    public static void addApiDetails(ExtentTest test, String method, String url, String requestBody, String response) {
        if (test != null) {
            test.info("<b>API Request Details:</b>");
            test.info("<pre><b>Method:</b> " + method + "</pre>");
            test.info("<pre><b>URL:</b> " + url + "</pre>");
            if (requestBody != null && !requestBody.isEmpty()) {
                test.info("<pre><b>Request Body:</b><br>" + formatJson(requestBody) + "</pre>");
            }
            test.info("<pre><b>Response:</b><br>" + formatJson(response) + "</pre>");
        }
    }
    
    /**
     * Add GraphQL query details to report
     */
    public static void addGraphQLDetails(ExtentTest test, String query, Object variables, String response) {
        if (test != null) {
            test.info("<b>GraphQL Query Details:</b>");
            test.info("<pre><b>Query:</b><br>" + formatCode(query, "graphql") + "</pre>");
            if (variables != null) {
                test.info("<pre><b>Variables:</b><br>" + formatJson(variables.toString()) + "</pre>");
            }
            test.info("<pre><b>Response:</b><br>" + formatJson(response) + "</pre>");
        }
    }
    
    /**
     * Add test data information to report
     */
    public static void addTestData(ExtentTest test, String testName, Object testData) {
        if (test != null) {
            test.info("<b>Test Data:</b>");
            test.info("<pre><b>Test Name:</b> " + testName + "</pre>");
            if (testData != null) {
                test.info("<pre><b>Data:</b><br>" + formatJson(testData.toString()) + "</pre>");
            }
        }
    }
    
    /**
     * Add performance metrics to report
     */
    public static void addPerformanceMetrics(ExtentTest test, long responseTime, int statusCode) {
        if (test != null) {
            test.info("<b>Performance Metrics:</b>");
            test.info("<pre><b>Response Time:</b> " + responseTime + " ms</pre>");
            test.info("<pre><b>Status Code:</b> " + statusCode + "</pre>");
            
            // Add performance warning if response time is high
            if (responseTime > 5000) {
                test.warning("Slow response detected: " + responseTime + " ms");
            } else if (responseTime > 2000) {
                test.info("Moderate response time: " + responseTime + " ms");
            }
        }
    }
    
    /**
     * Add error analysis to report
     */
    public static void addErrorAnalysis(ExtentTest test, String errorMessage, String suggestion) {
        if (test != null) {
            test.warning("<b>Error Analysis:</b>");
            test.warning("<pre><b>Error:</b> " + errorMessage + "</pre>");
            if (suggestion != null && !suggestion.isEmpty()) {
                test.info("<pre><b>Suggestion:</b> " + suggestion + "</pre>");
            }
        }
    }
    
    private static String saveScreenshotToFile(String base64Data, String title) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = SCREENSHOT_DIR + "/" + title.replace(" ", "_") + "_" + timestamp + ".png";
            
            // Convert base64 to bytes and save
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            Files.write(new File(fileName).toPath(), imageBytes);
            
            return fileName;
        } catch (Exception e) {
            return "";
        }
    }
    
    private static String formatJson(String json) {
        if (json == null) return "";
        
        // Simple JSON formatting - in real implementation, use proper JSON library
        return json.replace(",", ",<br>")
                 .replace("{", "{<br>")
                 .replace("}", "<br>}")
                 .replace("[", "[<br>")
                 .replace("]", "<br>]");
    }
    
    private static String formatCode(String code, String language) {
        return "<code class=\"language-" + language + "\">" + code + "</code>";
    }
    
    /**
     * Create a summary table for test results
     */
    public static String createSummaryTable(int total, int passed, int failed, int skipped) {
        StringBuilder table = new StringBuilder();
        table.append("<table border='1' style='border-collapse: collapse; width: 100%; color: #e0e0e0; border-color: #444;'>");
        table.append("<tr style='background-color: #333;'>");
        table.append("<th style='padding: 8px; text-align: left;'>Metric</th>");
        table.append("<th style='padding: 8px; text-align: left;'>Count</th>");
        table.append("<th style='padding: 8px; text-align: left;'>Percentage</th>");
        table.append("</tr>");
        
        table.append("<tr>");
        table.append("<td style='padding: 8px;'>Total Tests</td>");
        table.append("<td style='padding: 8px;'>" + total + "</td>");
        table.append("<td style='padding: 8px;'>100%</td>");
        table.append("</tr>");
        
        table.append("<tr>");
        table.append("<td style='padding: 8px; color: green;'>Passed</td>");
        table.append("<td style='padding: 8px; color: green;'>" + passed + "</td>");
        table.append("<td style='padding: 8px; color: green;'>" + getPercentage(passed, total) + "%</td>");
        table.append("</tr>");
        
        table.append("<tr>");
        table.append("<td style='padding: 8px; color: red;'>Failed</td>");
        table.append("<td style='padding: 8px; color: red;'>" + failed + "</td>");
        table.append("<td style='padding: 8px; color: red;'>" + getPercentage(failed, total) + "%</td>");
        table.append("</tr>");
        
        table.append("<tr>");
        table.append("<td style='padding: 8px; color: orange;'>Skipped</td>");
        table.append("<td style='padding: 8px; color: orange;'>" + skipped + "</td>");
        table.append("<td style='padding: 8px; color: orange;'>" + getPercentage(skipped, total) + "%</td>");
        table.append("</tr>");
        
        table.append("</table>");
        return table.toString();
    }
    
    private static String getPercentage(int value, int total) {
        if (total == 0) return "0";
        return String.format("%.1f", (value * 100.0) / total);
    }
}
