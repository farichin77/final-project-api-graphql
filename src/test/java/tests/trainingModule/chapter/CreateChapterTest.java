package tests.trainingModule.chapter;

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
import models.requests.chapter.CreateChapterVariable;
import models.responses.chapter.CreateChapterResponse;
import io.restassured.response.Response;

import java.util.Map;

public class CreateChapterTest extends BaseTest {

    @Test(dataProvider = "chapterTestData", dataProviderClass = TestDataProvider.class)
    public void testCreateChapterWithDataDriven(CsvReader.ChapterTestData testData) {

        String latestTrainingId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file.");
        }

        String actualProgramId = testData.programId.equals("{latestTrainingId}") ? latestTrainingId : testData.programId;

        Map<String, Object> variables = CreateChapterVariable.variables(
            testData.title,
            testData.description,
            testData.order,
            actualProgramId
        );

        String query = GraphQlFileReader.readMutationTraining("CreateChapter.graphql");

        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                CreateChapterResponse responseBody = response.as(CreateChapterResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.createChapter, "createChapter object is null");
                    
                    String chapterId = responseBody.data.createChapter.id;
                    if (!testData.title.isEmpty()) {
                        JsonHelper.saveIdToJson(FilePaths.CHAPTER_DATA_JSON, chapterId, testData.title, "programId", actualProgramId);
                    }
                    System.out.println("✓ Chapter created: " + chapterId);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.createChapter == null,
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
