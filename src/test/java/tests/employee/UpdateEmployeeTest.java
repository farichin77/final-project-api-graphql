package tests.employee;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.TestDataProvider;
import utils.GraphQlFileReader;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class UpdateEmployeeTest extends BaseTest {

    @Test(dataProvider = "updateEmployeeTestData", dataProviderClass = TestDataProvider.class)
    public void testUpdateEmployeeWithDataDriven(CsvReader.UpdateEmployeeTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get employee ID from JSON file
        String employeeId = getEmployeeIdFromJson();
        if (employeeId == null) {
            Assert.fail("No employee ID found in JSON file. Run EmployeeDataDrivenTest first.");
        }
        
        // Replace placeholder with actual employee ID
        String actualId = testData.id.equals("{lastCreatedId}") ? employeeId : testData.id;
        
        // Replace timestamp placeholder in email
        String processedEmail = testData.email.replace("{timestamp}", String.valueOf(System.currentTimeMillis()));
        
        System.out.println("=== Updating Employee ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Employee ID: " + actualId);
        System.out.println("New Name: " + testData.name);
        System.out.println("New Email: " + processedEmail);

        // Prepare variables for employee update
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("name", testData.name);
        inputMap.put("employeeId", testData.employeeId);
        inputMap.put("email", processedEmail);
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

        String query = GraphQlFileReader.readMutation("UpdateEmployee.graphql");

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
            String filePath = "src/test/resources/employee-data/employee-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read entire file
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            } catch (Exception e) {
                System.out.println("⚠ JSON file not found: " + e.getMessage());
                // Fallback to TXT file
                return getEmployeeIdFromTxt();
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                // Fallback to TXT file
                return getEmployeeIdFromTxt();
            }
            
            // Parse JSON and get the latest employee ID
            if (content.startsWith("[") && content.endsWith("]")) {
                String arrayContent = content.substring(1, content.length() - 1);
                String[] employeeObjects = arrayContent.split("\\},\\{");
                
                String latestEmployeeId = null;
                long latestTimestamp = 0;
                
                for (String employeeObj : employeeObjects) {
                    // Clean up object string
                    if (!employeeObj.startsWith("{")) {
                        employeeObj = "{" + employeeObj;
                    }
                    if (!employeeObj.endsWith("}")) {
                        employeeObj = employeeObj + "}";
                    }
                    
                    // Extract ID
                    int idStart = employeeObj.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6;
                        int idEnd = employeeObj.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String employeeId = employeeObj.substring(idStart, idEnd);
                            
                            // Extract timestamp
                            int timestampStart = employeeObj.indexOf("\"timestamp\":");
                            if (timestampStart != -1) {
                                timestampStart += 12;
                                int timestampEnd = employeeObj.indexOf("}", timestampStart);
                                if (timestampEnd != -1) {
                                    try {
                                        long timestamp = Long.parseLong(employeeObj.substring(timestampStart, timestampEnd));
                                        if (timestamp > latestTimestamp) {
                                            latestTimestamp = timestamp;
                                            latestEmployeeId = employeeId;
                                        }
                                    } catch (NumberFormatException e) {
                                        // Ignore timestamp parsing errors
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (latestEmployeeId != null) {
                    System.out.println("✓ Found latest employee ID from JSON: " + latestEmployeeId);
                    return latestEmployeeId;
                } else {
                    // Fallback to TXT file
                    return getEmployeeIdFromTxt();
                }
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Failed to read latest employee ID from JSON: " + e.getMessage());
            // Fallback to TXT file
            return getEmployeeIdFromTxt();
        }
        return null;
    }

    private String getEmployeeIdFromTxt() {
        try {
            String filePath = "src/test/resources/employee-data/employee-id.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String employeeId = reader.readLine();
            reader.close();
            
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                System.out.println("✓ Read employee ID from TXT file: " + employeeId);
                return employeeId;
            }
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read employee ID from TXT file: " + e.getMessage());
        }
        return null;
    }
}
