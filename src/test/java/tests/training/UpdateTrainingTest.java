package tests.training;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.client.AuthSession;
import qa.services.AuthService;
import qa.client.GraphQlClient;
import qa.utils.CsvReader;
import qa.utils.CsvReader.UpdateTrainingTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class UpdateTrainingTest {

    @BeforeMethod
    public void setUp() {
        // Reset session before each test
        AuthSession.setSessionCookie(null);
    }

    @DataProvider(name = "updateTrainingTestData")
    public Object[][] getUpdateTrainingTestData() {
        var testDataList = CsvReader.readUpdateTrainingTestData("test-data/update-training-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "updateTrainingTestData")
    public void testUpdateTrainingWithDataDriven(UpdateTrainingTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get training ID from JSON file
        String trainingId = getTrainingIdFromJson();
        if (trainingId == null) {
            Assert.fail("No training ID found in JSON file. Run TrainingDataDrivenTest first.");
        }
        
        // Replace placeholder with actual training ID
        String actualId = testData.id.equals("{lastCreatedId}") ? trainingId : testData.id;
        
        System.out.println("=== Updating Training ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Training ID: " + actualId);
        System.out.println("New Title: " + testData.title);
        System.out.println("New Description: " + testData.description);

        // Prepare variables for training update
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", testData.title);
        inputMap.put("description", testData.description);
        inputMap.put("type", testData.type);
        inputMap.put("isSequential", testData.isSequential);

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", actualId);
        variables.put("input", inputMap);

        String query = "mutation updateProgram($id: String!, $input: ProgramInput!) {\n" +
            "  updateProgram(id: $id, input: $input) {\n" +
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
                    // Verify successful training update
                    Assert.assertFalse(responseBody.contains("errors"), 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"updateProgram\""), 
                        "Response should contain updateProgram for scenario: " + testData.scenario);
                    Assert.assertTrue(responseBody.contains("\"id\":\"" + actualId + "\""), 
                        "Response should contain training ID for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Training updated successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify training update fails (should have errors)
                    Assert.assertTrue(responseBody.contains("errors") || !responseBody.contains("\"updateProgram\""),
                        "Training update should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Training update failed as expected: " + testData.scenario);
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

    private String getTrainingIdFromJson() {
        try {
            String filePath = "src/test/resources/training-data/training-id.json";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String jsonContent = reader.readLine();
            reader.close();
            
            // Extract ID from JSON: {"id":"uuid-here","title":"training-name","timestamp":1234567890}
            int idStart = jsonContent.indexOf("\"id\":\"") + 6;
            int idEnd = jsonContent.indexOf("\"", idStart);
            
            if (idStart > 5 && idEnd > idStart) {
                String trainingId = jsonContent.substring(idStart, idEnd);
                System.out.println("✓ Read training ID from JSON: " + trainingId);
                return trainingId;
            }
            
        } catch (IOException e) {
            System.out.println("⚠ Failed to read training ID from JSON: " + e.getMessage());
        }
        return null;
    }
}
