package tests.employee;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.client.AuthSession;
import qa.services.AuthService;
import qa.client.GraphQlClient;
import qa.utils.CsvReader;
import qa.utils.CsvReader.UpdateEmployeeTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class UpdateEmployeeTest {

    @BeforeMethod
    public void setUp() {
        // Reset session before each test
        AuthSession.setSessionCookie(null);
    }

    @DataProvider(name = "updateEmployeeTestData")
    public Object[][] getUpdateEmployeeTestData() {
        var testDataList = CsvReader.readUpdateEmployeeTestData("test-data/update-employee-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "updateEmployeeTestData")
    public void testUpdateEmployeeWithDataDriven(UpdateEmployeeTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get employee ID from JSON file
        String employeeId = getEmployeeIdFromJson();
        if (employeeId == null) {
            Assert.fail("No employee ID found in JSON file. Run EmployeeDataDrivenTest first.");
        }
        
        // Replace placeholder with actual employee ID
        String actualId = testData.id.equals("{lastCreatedId}") ? employeeId : testData.id;
        
        System.out.println("=== Updating Employee ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Employee ID: " + actualId);
        System.out.println("New Name: " + testData.name);
        System.out.println("New Email: " + testData.email);

        // Prepare variables for employee update
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("name", testData.name);
        inputMap.put("employeeId", testData.employeeId);
        inputMap.put("email", testData.email);
        inputMap.put("phoneNumber", testData.phoneNumber);
        inputMap.put("divisionId", testData.divisionId);
        inputMap.put("employeeRole", testData.employeeRole);
        inputMap.put("angkatanId", Integer.parseInt(testData.angkatanId));
        inputMap.put("gender", testData.gender);
        inputMap.put("dateOfBirth", testData.dateOfBirth);
        inputMap.put("address", testData.address);
        inputMap.put("nik", testData.nik);
        inputMap.put("npwp", testData.npwp);

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", actualId);
        variables.put("input", inputMap);

        String query = "mutation updateEmployee($input: EmployeeInput!, $id: String!) {\n" +
            "  updateEmployee(input: $input, id: $id) {\n" +
            "    id\n" +
            "    __typename\n" +
            "  }\n" +
            "}";

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                String responseBody = response.getBody().asString();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful employee update
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"updateEmployee\""), 
                        "Response should contain updateEmployee for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"id\":\"" + actualId + "\""), 
                        "Response should contain employee ID for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Employee updated successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify employee update fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"updateEmployee\""),
                        "Employee update should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Employee update failed as expected: " + testData.scenario);
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

    private String getEmployeeIdFromJson() {
        try {
            String filePath = "src/test/resources/employee-data/employee-id.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String employeeId = reader.readLine();
            reader.close();
            
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                System.out.println("✓ Read employee ID from file: " + employeeId);
                return employeeId;
            }
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read employee ID from file: " + e.getMessage());
        }
        return null;
    }
}
