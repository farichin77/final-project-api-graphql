package tests.employee;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import models.requests.employee.AddEmployeeVariable;
import models.responses.employee.AddEmployeeResponse;
import qa.client.AuthSession;
import qa.services.AuthService;
import qa.client.GraphQlClient;
import qa.utils.CsvReader;
import qa.utils.CsvReader.EmployeeTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

public class CreateEmployeeTest {

    // Static variable to store Ahmad ID for delete test
    public static String AHMAD_EMPLOYEE_ID = null;

    @BeforeMethod
    public void beforeTest() {
        // Reset session before each test
        AuthSession.setSessionCookie(null);
    }

    @DataProvider(name = "employeeTestData")
    public Object[][] getEmployeeTestData() {
        var testDataList = CsvReader.readEmployeeTestData("test-data/employee-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "employeeTestData")
    public void testAddEmployeeWithDataDriven(EmployeeTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();

        // Call the employee service with the test data
        Map<String, Object> variables = AddEmployeeVariable.variables(
            testData.name,
            testData.employeeId,
            testData.email,
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

        String query = "mutation createEmployee($input: EmployeeInput!) {\n" +
            "  createEmployee(input: $input) {\n" +
            "    id\n" +
            "    __typename\n" +
            "  }\n" +
            "}";

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                AddEmployeeResponse responseBody = response.as(AddEmployeeResponse.class);
                
                // Check if response has GraphQL errors
                boolean hasGraphQLErrors = responseBody.errors != null && responseBody.errors.size() > 0;

                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful employee creation
                    Assert.assertFalse(hasGraphQLErrors, 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, 
                        "Response data should not be null for scenario: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data.createEmployee, 
                        "CreateEmployee should not be null for scenario: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data.createEmployee.id,
                        "Employee ID should be generated for scenario: " + testData.scenario);
                    
                    // Store Ahmad ID for delete test
                    if ("Ahmad".equals(testData.name)) {
                        AHMAD_EMPLOYEE_ID = responseBody.data.createEmployee.id;
                        System.out.println("✓ Stored Ahmad ID for delete test: " + AHMAD_EMPLOYEE_ID);
                        
                        // Save employee ID to JSON file
                        saveEmployeeIdToJson(AHMAD_EMPLOYEE_ID, testData.name);
                    }
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify employee creation fails (should have errors or no id)
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.createEmployee == null,
                        "Employee creation should fail for scenario: " + testData.scenario);
                }
            } catch (Exception e) {
                if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    System.out.println("Expected failure for: " + testData.scenario + " - " + e.getMessage());
                } else {
                    throw e;
                }
            }
        } else {
            // If status code is not 200, the request failed
            if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                System.out.println("Test expected to fail and it did with status: " + response.statusCode());
            } else {
                System.out.println("Scenario: " + testData.scenario + " - Status: " + response.statusCode());
            }
        }
    }

    private void saveEmployeeIdToJson(String employeeId, String employeeName) {
        try {
            String filePath = "src/test/resources/employee-data/employee-id.txt";
            FileWriter file = new FileWriter(filePath);
            file.write(employeeId);
            file.close();
            
            System.out.println("✓ Employee ID saved to file: " + filePath);
            System.out.println("✓ Employee ID: " + employeeId + " (Name: " + employeeName + ")");
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to save employee ID to file: " + e.getMessage());
        }
    }
}
