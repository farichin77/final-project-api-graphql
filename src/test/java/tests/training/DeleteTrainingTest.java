package tests.training;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.client.AuthSession;
import qa.services.AuthService;
import qa.client.GraphQlClient;
import qa.utils.CsvReader;
import qa.utils.CsvReader.DeleteTrainingTestData;
import io.restassured.response.Response;

import java.util.Map;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class DeleteTrainingTest {

    @BeforeMethod
    public void setUp() {
        // Reset session before each test
        AuthSession.setSessionCookie(null);
    }

    @DataProvider(name = "deleteTrainingTestData")
    public Object[][] getDeleteTrainingTestData() {
        var testDataList = CsvReader.readDeleteTrainingTestData("test-data/delete-training-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "deleteTrainingTestData")
    public void testDeleteTrainingWithDataDriven(DeleteTrainingTestData testData) {
        // First authenticate to get valid session
        AuthService.postLogin();
        
        // Get training ID from JSON file
        String trainingId = getTrainingIdFromJson();
        if (trainingId == null) {
            Assert.fail("No training ID found in JSON file. Run TrainingDataDrivenTest first.");
        }
        
        // Replace placeholder with actual training ID
        String actualId = testData.id.equals("{lastCreatedId}") ? trainingId : testData.id;
        
        System.out.println("=== Deleting Training ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Training ID: " + actualId);

        // Execute delete mutation
        Map<String, Object> variables = Map.of("id", actualId);
        String deletePayload = "mutation deleteProgram($id: String!) { deleteProgram(id: $id) }";
        Response deleteResponse = GraphQlClient.execute(deletePayload, variables);

        System.out.println("Delete Response Status: " + deleteResponse.statusCode());
        System.out.println("Delete Response Body: " + deleteResponse.asString());

        Assert.assertEquals(deleteResponse.statusCode(), 200, "Delete training should return 200");
        
        String deleteResponseBody = deleteResponse.getBody().asString();
        Assert.assertTrue(deleteResponseBody.contains("\"deleteProgram\":true"), 
            "deleteProgram should return true for successful deletion");
        
        System.out.println("✓ Training deleted successfully");
        System.out.println("✓ Test completed - Training ID read from JSON and deleted");
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
