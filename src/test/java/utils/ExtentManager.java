package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

public class ExtentManager {
    
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static final String REPORT_PATH = "test-output/Automation-API-Report.html";

    public static ExtentReports getInstance() {
        if (extent == null) {
            createInstance();
        }
        return extent;
    }

    private static ExtentReports createInstance() {
        // Create output directory if it doesn't exist
        File outputDir = new File("test-output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // Spark reporter
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        
        // Konfigurasi report
        spark.config().setDocumentTitle("GraphQL API - Automation Report");
        spark.config().setReportName("API Testing Execution Results");
        spark.config().setTheme(Theme.DARK);
        spark.config().setEncoding("UTF-8");
        spark.config().setTimeStampFormat("EEEE, dd MMM yyyy HH:mm:ss");
        
        // Configure view order
        spark.viewConfigurer()
            .viewOrder()
            .as(new ViewName[] { ViewName.DASHBOARD, ViewName.TEST, ViewName.EXCEPTION, ViewName.LOG })
            .apply();
        
        // Setup ExtentReports
        extent = new ExtentReports();
        extent.attachReporter(spark);
        
        // System info
        extent.setSystemInfo("Tester", "Ahmad Farichin");
        extent.setSystemInfo("Environment", "Test");
        extent.setSystemInfo("Base URL", "https://lmsb2b.do.dibimbing.id");
        extent.setSystemInfo("Framework", "Rest Assured + TestNG + Gradle");
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("API Version", "v1.0.0");
        
        return extent;
    }

    public static void createTest(String testName, String description) {
        ExtentTest test = getInstance().createTest(testName, description);
        extentTest.set(test);
    }
    
    public static void createTest(String testName, String description, String category) {
        ExtentTest test = getInstance().createTest(testName, description).assignCategory(category);
        extentTest.set(test);
    }

    public static ExtentTest getTest() {
        return extentTest.get();
    }

    public static void removeTest() {
        extentTest.remove();
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
            System.out.println("=== Extent Report Generated ===");
            System.out.println("Report Location: " + new File(REPORT_PATH).getAbsolutePath());
        }
    }
    
    public static String getReportPath() {
        return REPORT_PATH;
    }
    
    // Method to add custom logs with different levels
    public static void logInfo(String message) {
        if (getTest() != null) {
            getTest().info(message);
        }
    }
    
    public static void logPass(String message) {
        if (getTest() != null) {
            getTest().pass(message);
        }
    }
    
    public static void logFail(String message) {
        if (getTest() != null) {
            getTest().fail(message);
        }
    }
    
    public static void logWarning(String message) {
        if (getTest() != null) {
            getTest().warning(message);
        }
    }
    
    public static void logDebug(String message) {
        if (getTest() != null) {
            getTest().info(message);
        }
    }
}
