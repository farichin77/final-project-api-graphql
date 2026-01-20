package core;

import client.AuthSession;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import utils.ExtentManager;

public class BaseTest {

    @BeforeSuite
    public void setUpSuite() {
        ExtentManager.getInstance();
    }

    @AfterSuite
    public void tearDownSuite() {
        ExtentManager.flush();
    }

    @AfterMethod
    public void tearDownTest() {
        ExtentManager.removeTest();
    }
}

