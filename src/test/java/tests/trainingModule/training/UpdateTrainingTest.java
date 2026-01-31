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
import models.requests.training.UpdateTrainingVariable;
import models.responses.training.UpdateProgramResponse;
import io.restassured.response.Response;

import java.util.Map;

public class UpdateTrainingTest extends BaseTest {

    @Test(dataProvider = "updateTrainingTestData", dataProviderClass = TestDataProvider.class)
    public void testUpdateTrainingWithDataDriven(CsvReader.UpdateTrainingTestData testData) {
        
        String trainingId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        if (trainingId == null) {
            Assert.fail("No training ID found in JSON file.");
        }
        
        String actualId = "{lastCreatedId}".equals(testData.id) ? trainingId : testData.id;
        
        Map<String, Object> variables = UpdateTrainingVariable.variables(
            actualId,
            testData.title,
            testData.description,
            testData.type,
            testData.isSequential
        );

        String query = GraphQlFileReader.readMutationTraining("UpdateTraining.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                UpdateProgramResponse responseBody = response.as(UpdateProgramResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.updateProgram, "updateProgram object is null");
                    Assert.assertEquals(responseBody.data.updateProgram.id, actualId, "ID mismatch");
                    
                    System.out.println("✓ Training updated: " + actualId);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.updateProgram == null,
                        "Update should have failed: " + testData.scenario);
                    System.out.println("✓ Failure confirmed as expected");
                }
            } catch (Exception e) {
                if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    System.out.println("Expected failure: " + e.getMessage());
                } else {
                    throw e;
                }
            }
        } else {
            if (!"FAIL".equalsIgnoreCase(testData.expectedResult)) {
                System.out.println("Unexpected status: " + response.statusCode());
            }
        }
    }
}
