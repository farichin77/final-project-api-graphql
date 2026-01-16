package core;

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
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription() != null ? 
                           result.getMethod().getDescription() : 
                           "Test: " + testName;
        ExtentManager.createTest(testName, description);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentManager.getTest().log(Status.PASS, "Test Passed");
        ExtentManager.getTest().log(Status.INFO, "Test completed successfully");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentManager.getTest().log(Status.FAIL, "Test Failed");
        ExtentManager.getTest().log(Status.FAIL, "Failure Reason: " + result.getThrowable().getMessage());
        
        String methodName = result.getMethod().getMethodName();
        String exceptionMessage = result.getThrowable().getMessage();
        ExtentManager.getTest().fail(MarkupHelper.createLabel("Test Case Failed: " + methodName, ExtentColor.RED));
        ExtentManager.getTest().fail(exceptionMessage);
        
        if (result.getThrowable() != null) {
            ExtentManager.getTest().fail(result.getThrowable());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentManager.getTest().log(Status.SKIP, "Test Skipped");
        ExtentManager.getTest().log(Status.SKIP, "Skip Reason: " + 
                          (result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown"));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        ExtentManager.getTest().log(Status.WARNING, "Test Failed but within success percentage");
    }
}
