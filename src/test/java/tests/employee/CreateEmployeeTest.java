package tests.employee;

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
import io.restassured.response.Response;

import java.util.Map;
import java.io.IOException;

public class CreateEmployeeTest extends BaseTest {

    // Static variable to store Ahmad ID for delete test
    public static String AHMAD_EMPLOYEE_ID = null;

    @Test(dataProvider = "employeeTestData", dataProviderClass = TestDataProvider.class)
    public void testAddEmployeeWithDataDriven(CsvReader.EmployeeTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();

        // Call employee service with test data
        // Replace timestamp placeholder in email
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

        String query = GraphQlFileReader.readMutation("CreateEmployee.graphql");
        
        System.out.println("GraphQL Query: " + query);
        System.out.println("Variables: " + variables);

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
                    } else if ("Budi".equals(testData.name)) {
                        // Save Budi ID to JSON as well
                        saveEmployeeIdToJson(responseBody.data.createEmployee.id, testData.name);
                    } else if ("Citra".equals(testData.name)) {
                        // Save Citra ID to JSON as well
                        saveEmployeeIdToJson(responseBody.data.createEmployee.id, testData.name);
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
            String filePath = "src/test/resources/employee-data/employee-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read existing content
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            } catch (Exception e) {
                // File doesn't exist, create new
                jsonContent.append("[]");
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                content = "[]";
            }
            
            // Parse and add new employee
            if (content.startsWith("[") && content.endsWith("]")) {
                String arrayContent = content.substring(1, content.length() - 1);
                if (arrayContent.isEmpty()) {
                    arrayContent = "";
                } else {
                    arrayContent += ",";
                }
                arrayContent += "{\"id\":\"" + employeeId + "\",\"name\":\"" + employeeName + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
                content = "[" + arrayContent + "]";
            }
            
            // Write back to file
            try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
                writer.write(content);
            }
            
            System.out.println("✓ Employee ID saved to JSON file: " + filePath);
            System.out.println("✓ Employee ID: " + employeeId + " (Name: " + employeeName + ")");
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to save employee ID to JSON file: " + e.getMessage());
        }
    }
}
