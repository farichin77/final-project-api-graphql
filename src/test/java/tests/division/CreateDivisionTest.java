package tests.division;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.CsvReader.DivisionTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

public class CreateDivisionTest extends BaseTest {


    @DataProvider(name = "divisionTestData")
    public Object[][] getDivisionTestData() {
        var testDataList = CsvReader.readDivisionTestData("test-data/division-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "divisionTestData")
    public void testCreateDivisionWithDataDriven(DivisionTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();

        // Prepare variables for division creation
        Map<String, Object> variables = Map.of(
            "input", Map.of(
                "name", testData.name,
                "description", testData.description
            )
        );

        String query = "mutation createDivision($input: DivisionInput!) {\n" +
            "  createDivision(input: $input) {\n" +
            "    id\n" +
            "    code\n" +
            "    name\n" +
            "    description\n" +
            "    __typename\n" +
            "  }\n" +
            "}";

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                // Parse response manually since we don't have response classes
                String responseBody = response.getBody().asString();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful division creation
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"createDivision\""), 
                        "Response should contain createDivision for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"name\":\"" + testData.name + "\""), 
                        "Response should contain division name for scenario: " + testData.scenario);
                    
                    // Extract division ID and save to JSON
                    String divisionId = extractDivisionId(responseBody);
                    if (divisionId != null && !testData.name.isEmpty()) {
                        saveDivisionIdToJson(divisionId, testData.name);
                    }
                    
                    System.out.println("✓ Division created successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify division creation fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"createDivision\""),
                        "Division creation should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Division creation failed as expected: " + testData.scenario);
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

    private String extractDivisionId(String responseBody) {
        try {
            // Extract ID from JSON response: {"data":{"createDivision":{"id":"uuid-here",...}}}
            int idStart = responseBody.indexOf("\"id\":\"") + 6;
            int idEnd = responseBody.indexOf("\"", idStart);
            if (idStart > 5 && idEnd > idStart) {
                return responseBody.substring(idStart, idEnd);
            }
        } catch (Exception e) {
            System.out.println("Failed to extract division ID: " + e.getMessage());
        }
        return null;
    }

    private void saveDivisionIdToJson(String divisionId, String divisionName) {
        try {
            String filePath = "src/test/resources/employee-data/division-id.json";
            FileWriter file = new FileWriter(filePath);
            file.write("{\"id\":\"" + divisionId + "\",\"name\":\"" + divisionName + "\",\"timestamp\":" + System.currentTimeMillis() + "}");
            file.close();
            
            System.out.println("✓ Division ID saved to JSON file: " + filePath);
            System.out.println("✓ Division ID: " + divisionId + " (Name: " + divisionName + ")");
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to save division ID to JSON: " + e.getMessage());
        }
    }
}
