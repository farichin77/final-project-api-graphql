package tests.auth;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import models.responses.login.LoginResponse;
import services.AuthService;
import utils.ApiResponse;
import utils.CsvReader;
import utils.GraphQlFileReader;

public class LoginTest {

    @DataProvider(name = "loginTestData")
    public Object[][] getLoginTestData() {
        var testDataList = CsvReader.readPositiveLoginTestData("test-data/auth/login-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "loginTestData")
    public void testLoginWithDataDriven(CsvReader.LoginTestData testData) {
        String query = GraphQlFileReader.readMutationAuth("Login.graphql");
        
        io.restassured.response.Response rawResponse = AuthService.postLoginRaw(
            testData.email,
            testData.password,
            testData.companyId
        );

        ApiResponse<LoginResponse> response = new ApiResponse<>(
            rawResponse.getStatusCode(),
            rawResponse.getHeaders(),
            rawResponse.as(LoginResponse.class)
        );

        LoginResponse responseBody = response.getResponseBody();

        if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
            // Verify successful login - just check that we got a session cookie
            Assert.assertNotNull(rawResponse.getCookie("sid_b2b"), 
                "Login should succeed for scenario: " + testData.scenario);
        } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
            // Verify login fails
            if (responseBody.data != null) {
                Assert.assertNull(responseBody.data.login.user, 
                    "Login should fail for scenario: " + testData.scenario);
                Assert.assertNotNull(responseBody.data.login.errors,
                    "Errors should be present for scenario: " + testData.scenario);
                Assert.assertTrue(responseBody.data.login.errors.size() > 0,
                    "Error list should not be empty for scenario: " + testData.scenario);
            } else {
                // When data is null, check status code
                Assert.assertTrue(rawResponse.getStatusCode() != 200, 
                    "Login should fail for scenario: " + testData.scenario + 
                    " (Status: " + rawResponse.getStatusCode() + ")");
            }
        }
    }
}
