package tests.trainingModule.training;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.TestDataProvider;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import utils.FilePaths;
import models.responses.CommonDeleteResponse;
import io.restassured.response.Response;

import java.util.Map;

public class DeleteTrainingTest extends BaseTest {

    @Test(dataProvider = "deleteTrainingTestData", dataProviderClass = TestDataProvider.class)
    public void testDeleteTrainingWithDataDriven(CsvReader.DeleteTrainingTestData testData) {

        String trainingId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        if (trainingId == null) {
            Assert.fail("No training ID found in JSON file. Run CreateTrainingTest first.");
        }
        String actualId = testData.id.equals("{lastCreatedId}") ? trainingId : testData.id;
        
        System.out.println("=== Deleting Training ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Training ID: " + actualId);

        // Execute delete mutation
        Map<String, Object> variables = Map.of("id", actualId);
        String deletePayload = GraphQlFileReader.readMutationTraining("DeleteTraining.graphql");
        Response response = GraphQlClient.execute(deletePayload, variables);

        System.out.println("Delete Response Status: " + response.statusCode());
        System.out.println("Delete Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                CommonDeleteResponse deleteResponse = response.as(CommonDeleteResponse.class);
                boolean isDeleted = deleteResponse.data != null && Boolean.TRUE.equals(deleteResponse.data.deleteProgram);
                
                Assert.assertTrue(isDeleted, "deleteProgram should return true for successful deletion");
                System.out.println("✓ Training deleted successfully");
            } catch (Exception e) {
                Assert.fail("Failed to parse delete response: " + e.getMessage());
            }
        } else {
            Assert.fail("Delete training should return 200. Got: " + response.statusCode());
        }
    }
}
