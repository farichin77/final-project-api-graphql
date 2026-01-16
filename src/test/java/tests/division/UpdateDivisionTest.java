package tests.division;

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
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class UpdateDivisionTest extends BaseTest {

    @Test(dataProvider = "updateDivisionTestData", dataProviderClass = TestDataProvider.class)
    public void testUpdateDivisionWithDataDriven(CsvReader.UpdateDivisionTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get division ID from JSON file
        String divisionId = getDivisionIdFromJson();
        if (divisionId == null) {
            Assert.fail("No division ID found in JSON file. Run DivisionDataDrivenTest first.");
        }
        
        // Replace placeholder with actual division ID
        String actualId = testData.id.equals("{lastCreatedId}") ? divisionId : testData.id;
        
        System.out.println("=== Updating Division ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Division ID: " + actualId);
        System.out.println("New Name: " + testData.name);
        System.out.println("New Description: " + testData.description);

        // Prepare variables for division update
        Map<String, Object> variables = Map.of(
            "id", actualId,
            "input", Map.of(
                "name", testData.name,
                "description", testData.description
            )
        );

        String query = GraphQlFileReader.readMutation("UpdateDivision.graphql");

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                String responseBody = response.getBody().asString();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful division update
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"updateDivision\""), 
                        "Response should contain updateDivision for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"id\":\"" + actualId + "\""), 
                        "Response should contain division ID for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Division updated successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify division update fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"updateDivision\""),
                        "Division update should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Division update failed as expected: " + testData.scenario);
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

    private String getDivisionIdFromJson() {
        try {
            String filePath = "src/test/resources/employee-data/division-id.json";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String jsonContent = reader.readLine();
            reader.close();
            
            // Extract ID from JSON: {"id":"uuid-here","name":"division-name","timestamp":1234567890}
            int idStart = jsonContent.indexOf("\"id\":\"") + 6;
            int idEnd = jsonContent.indexOf("\"", idStart);
            
            if (idStart > 5 && idEnd > idStart) {
                String divisionId = jsonContent.substring(idStart, idEnd);
                System.out.println("✓ Read division ID from JSON: " + divisionId);
                return divisionId;
            }
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read division ID from JSON: " + e.getMessage());
        }
        return null;
    }
}
