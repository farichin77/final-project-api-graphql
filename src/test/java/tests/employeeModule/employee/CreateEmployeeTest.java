package tests.employeeModule.employee;

import core.BaseTest;
import org.testng.Assert;

import org.testng.annotations.Test;
import models.requests.employee.AddEmployeeVariable;
import models.responses.employee.AddEmployeeResponse;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.TestDataProvider;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import utils.FilePaths;
import io.restassured.response.Response;

import java.util.Map;

public class CreateEmployeeTest extends BaseTest {

    public static String AHMAD_EMPLOYEE_ID = null;

    @Test(dataProvider = "employeeTestData", dataProviderClass = TestDataProvider.class)
    public void testAddEmployeeWithDataDriven(CsvReader.EmployeeTestData testData) {

        String processedEmail = testData.email.replace("{timestamp}", String.valueOf(System.currentTimeMillis()));
        
        Map<String, Object> variables = AddEmployeeVariable.variables(
            testData.name,
            testData.employeeId,
            processedEmail,
            testData.phoneNumber,
            testData.divisionId,
            testData.employeeRole,
            testData.angkatanId,
            testData.gender,
            testData.dateOfBirth,
            testData.address,
            testData.nik,
            testData.npwp
        );

        String query = GraphQlFileReader.readMutationEmployee("CreateEmployee.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                AddEmployeeResponse responseBody = response.as(AddEmployeeResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();

                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.createEmployee, "createEmployee object is null");
                    
                    String employeeId = responseBody.data.createEmployee.id;
                    
                    if ("Ahmad".equals(testData.name)) {
                        AHMAD_EMPLOYEE_ID = employeeId;
                        JsonHelper.saveIdToJson(FilePaths.EMPLOYEE_DATA_JSON, AHMAD_EMPLOYEE_ID, testData.name, null, null);
                    }

                    System.out.println("Employee created: " + employeeId);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.createEmployee == null,
                        "Creation should have failed: " + testData.scenario);
                    System.out.println("Failure confirmed as expected");
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
