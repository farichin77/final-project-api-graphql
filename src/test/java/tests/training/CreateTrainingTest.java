package tests.training;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.CsvReader.TrainingTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;

public class CreateTrainingTest extends BaseTest {


    @DataProvider(name = "trainingTestData")
    public Object[][] getTrainingTestData() {
        var testDataList = CsvReader.readTrainingTestData("test-data/training-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "trainingTestData")
    public void testCreateTrainingWithDataDriven(TrainingTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();

        // Prepare variables for training creation
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", testData.title);
        inputMap.put("description", testData.description);
        inputMap.put("type", testData.type);
        inputMap.put("isSequential", testData.isSequential);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", inputMap);

        String query = "mutation createProgram($input: ProgramInput!) {\n" +
            "  createProgram(input: $input) {\n" +
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
                String responseBody = response.getBody().asString();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful training creation
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"createProgram\""), 
                        "Response should contain createProgram for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"id\""), 
                        "Response should contain training ID for scenario: " + testData.scenario);
                    
                    // Extract training ID and save to JSON
                    String trainingId = extractTrainingId(responseBody);
                    if (trainingId != null && !testData.title.isEmpty()) {
                        saveTrainingIdToJson(trainingId, testData.title);
                    }
                    
                    System.out.println("✓ Training created successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify training creation fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"createProgram\""),
                        "Training creation should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Training creation failed as expected: " + testData.scenario);
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

    private String extractTrainingId(String responseBody) {
        try {
            // Extract ID from JSON response: {"data":{"createProgram":{"id":"uuid-here",...}}}
            int idStart = responseBody.indexOf("\"id\":\"") + 6;
            int idEnd = responseBody.indexOf("\"", idStart);
            if (idStart > 5 && idEnd > idStart) {
                return responseBody.substring(idStart, idEnd);
            }
        } catch (Exception e) {
            System.out.println("Failed to extract training ID: " + e.getMessage());
        }
        return null;
    }

    private void saveTrainingIdToJson(String trainingId, String trainingTitle) {
        try {
            String filePath = "src/test/resources/training-data/training-id.json";
            FileWriter file = new FileWriter(filePath);
            file.write("{\"id\":\"" + trainingId + "\",\"title\":\"" + trainingTitle + "\",\"timestamp\":" + System.currentTimeMillis() + "}");
            file.close();
            
            System.out.println("✓ Training ID saved to JSON file: " + filePath);
            System.out.println("✓ Training ID: " + trainingId + " (Title: " + trainingTitle + ")");
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to save training ID to JSON: " + e.getMessage());
        }
    }
}
