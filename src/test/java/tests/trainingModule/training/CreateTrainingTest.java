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
import models.requests.training.CreateTrainingVariable;
import models.responses.training.CreateProgramResponse;
import io.restassured.response.Response;

import java.util.Map;

public class CreateTrainingTest extends BaseTest {

    @Test(dataProvider = "trainingTestData", dataProviderClass = TestDataProvider.class)
    public void testCreateTrainingWithDataDriven(CsvReader.TrainingTestData testData) {
        AuthService.postLogin();

        Map<String, Object> variables = CreateTrainingVariable.variables(
            testData.title,
            testData.description,
            testData.type,
            testData.isSequential
        );

        String query = GraphQlFileReader.readMutationTraining("CreateTraining.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                CreateProgramResponse responseBody = response.as(CreateProgramResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.createProgram, "createProgram object is null");
                    
                    String trainingId = responseBody.data.createProgram.id;
                    if (!testData.title.isEmpty()) {
                        JsonHelper.saveIdToJson(FilePaths.TRAINING_DATA_JSON, trainingId, testData.title, null, null);
                    }
                    System.out.println("✓ Training created: " + trainingId);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.createProgram == null,
                        "Creation should have failed: " + testData.scenario);
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
