package tests.employeeModule.employee;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.TestDataProvider;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import utils.FilePaths;
import models.requests.employee.UpdateEmployeeVariable;
import models.responses.employee.UpdateEmployeeResponse;
import io.restassured.response.Response;

import java.util.Map;

public class UpdateEmployeeTest extends BaseTest {

    @Test(dataProvider = "updateEmployeeTestData", dataProviderClass = TestDataProvider.class)
    public void testUpdateEmployeeWithDataDriven(CsvReader.UpdateEmployeeTestData testData) {
        AuthService.postLogin();
        
        String employeeId = JsonHelper.getLatestIdFromJson(FilePaths.EMPLOYEE_DATA_JSON);
        if (employeeId == null) {
            Assert.fail("No employee ID found in JSON file.");
        }
        
        String actualId = "{lastCreatedId}".equals(testData.id) ? employeeId : testData.id;
        String processedEmail = testData.email.replace("{timestamp}", String.valueOf(System.currentTimeMillis()));
        
        Map<String, Object> variables = UpdateEmployeeVariable.variables(
            actualId,
            testData.name,
            testData.employeeId,
            processedEmail,
            testData.phoneNumber,
            testData.divisionId,
            testData.employeeRole,
            Integer.parseInt(testData.angkatanId),
            testData.gender,
            testData.dateOfBirth,
            testData.address,
            testData.nik,
            testData.npwp
        );

        String query = GraphQlFileReader.readMutationEmployee("UpdateEmployee.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                UpdateEmployeeResponse responseBody = response.as(UpdateEmployeeResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.updateEmployee, "updateEmployee object is null");
                    Assert.assertEquals(responseBody.data.updateEmployee.id, actualId, "ID mismatch");
                    
                    System.out.println("✓ Employee updated: " + actualId);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.updateEmployee == null,
                        "Update should have failed: " + testData.scenario);
                    System.out.println("✓ Failure confirmed as expected");
                }
            } catch (Exception e) {
                if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    System.out.println("Expected failure: " + e.getMessage());
                } else {
                    throw e;
                }
            }
        } else {
            if (!"FAIL".equalsIgnoreCase(testData.expectedResult)) {
                System.out.println("Unexpected status: " + response.statusCode());
            }
        }
    }
}
