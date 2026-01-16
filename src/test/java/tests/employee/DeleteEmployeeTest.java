package tests.employee;

import core.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import client.GraphQlClient;
import models.requests.employee.DeleteEmployeeVariable;
import models.responses.employee.DeleteEmployeeResponse;
import services.AuthService;
import utils.GraphQlFileReader;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;

public class DeleteEmployeeTest extends BaseTest {


    @Test
    public void testDeleteEmployeeFromJson() {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Read latest employee ID from JSON file
        String employeeId = getLatestEmployeeIdFromJson();
        if (employeeId == null) {
            Assert.fail("No employee ID found in JSON file. Run CreateEmployeeTest first.");
        }
        
        System.out.println("=== Deleting Latest Employee ===");
        System.out.println("Latest Employee ID: " + employeeId);

        // Execute delete mutation
        Map<String, Object> deleteVariables = DeleteEmployeeVariable.variables(employeeId);
        String deletePayload = GraphQlFileReader.readMutation("DeleteEmployee.graphql");
        Response deleteResponse = GraphQlClient.execute(deletePayload, deleteVariables);

        System.out.println("Delete Response Status: " + deleteResponse.statusCode());
        System.out.println("Delete Response Body: " + deleteResponse.asString());

        if (deleteResponse.statusCode() == 200) {
            String deleteResponseBody = deleteResponse.getBody().asString();
            
            if (deleteResponseBody.contains("\"deleteEmployee\":true")) {
                System.out.println("✓ Employee deleted successfully");
                
                // Remove deleted employee from JSON
                removeEmployeeIdFromJson(employeeId);
            } else {
                System.out.println("✗ Employee deletion failed");
            }
        } else {
            System.out.println("✗ Delete request failed with status: " + deleteResponse.statusCode());
        }
        
        System.out.println("✓ Test completed - Latest employee deleted");
    }

    private String getLatestEmployeeIdFromJson() {
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
                return readEmployeeIdFromFile();
            }
            
            String content = jsonContent.toString().trim();
            if (content.isEmpty()) {
                // Fallback to TXT file
                return readEmployeeIdFromFile();
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
                    return readEmployeeIdFromFile();
                }
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Failed to read latest employee ID from JSON: " + e.getMessage());
            // Fallback to TXT file
            return readEmployeeIdFromFile();
        }
        return null;
    }

    private void removeEmployeeIdFromJson(String employeeIdToRemove) {
        try {
            String filePath = "src/test/resources/employee-data/employee-id.json";
            StringBuilder jsonContent = new StringBuilder();
            
            // Read existing content
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }
            
            String content = jsonContent.toString().trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                String arrayContent = content.substring(1, content.length() - 1);
                String[] employeeObjects = arrayContent.split("\\},\\{");
                
                StringBuilder newContent = new StringBuilder();
                for (String employeeObj : employeeObjects) {
                    // Clean up object string
                    if (!employeeObj.startsWith("{")) {
                        employeeObj = "{" + employeeObj;
                    }
                    if (!employeeObj.endsWith("}")) {
                        employeeObj = employeeObj + "}";
                    }
                    
                    // Extract ID and check if it's the one to remove
                    int idStart = employeeObj.indexOf("\"id\":\"");
                    if (idStart != -1) {
                        idStart += 6;
                        int idEnd = employeeObj.indexOf("\"", idStart);
                        if (idEnd != -1) {
                            String currentEmployeeId = employeeObj.substring(idStart, idEnd);
                            
                            if (!currentEmployeeId.equals(employeeIdToRemove)) {
                                if (newContent.length() > 0) {
                                    newContent.append(",");
                                }
                                newContent.append(employeeObj);
                            }
                        }
                    }
                }
                
                // Write back to file
                try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
                    writer.write("[" + newContent.toString() + "]");
                }
                
                System.out.println("✓ Removed employee ID from JSON: " + employeeIdToRemove);
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Failed to remove employee ID from JSON: " + e.getMessage());
        }
    }

    private String readEmployeeIdFromFile() {
        try {
            String filePath = "src/test/resources/employee-data/employee-id.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String employeeId = reader.readLine();
            reader.close();
            
            System.out.println("✓ Read employee ID from file: " + employeeId);
            return employeeId;
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read employee ID from file: " + e.getMessage());
            return null;
        }
    }
}
