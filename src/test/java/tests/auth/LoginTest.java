package tests.auth;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import models.responses.login.LoginResponse;
import services.AuthService;
import utils.ApiResponse;
import utils.CsvReader;
import utils.CsvReader.LoginTestData;

public class LoginTest {

    @DataProvider(name = "loginTestData")
    public Object[][] getLoginTestData() {
        var testDataList = CsvReader.readPositiveLoginTestData("test-data/login-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "loginTestData")
    public void testLoginWithDataDriven(LoginTestData testData) {
        ApiResponse<LoginResponse> response = AuthService.postLogin(
            testData.email,
            testData.password,
            testData.companyId
        );

        LoginResponse responseBody = response.getResponseBody();

        if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
            // Verify successful login
            Assert.assertNotNull(responseBody.data.login.user, 
                "Login should succeed for scenario: " + testData.scenario);
            Assert.assertNotNull(responseBody.data.login.user.email,
                "Email should not be null for scenario: " + testData.scenario);
            Assert.assertEquals(responseBody.data.login.user.companyId, testData.companyId,
                "Company ID mismatch for scenario: " + testData.scenario);
        } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
            // Verify login fails
            Assert.assertNull(responseBody.data.login.user, 
                "Login should fail for scenario: " + testData.scenario);
            Assert.assertNotNull(responseBody.data.login.errors,
                "Errors should be present for scenario: " + testData.scenario);
            Assert.assertTrue(responseBody.data.login.errors.size() > 0,
                "Error list should not be empty for scenario: " + testData.scenario);
        }
    }
}
