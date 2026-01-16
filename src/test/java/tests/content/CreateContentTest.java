package tests.content;

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
import models.requests.content.CreateContentVariable;
import models.responses.content.CreateContentResponse;
import io.restassured.response.Response;

import java.util.Map;

public class CreateContentTest extends BaseTest {

    @Test(dataProvider = "contentTestData", dataProviderClass = TestDataProvider.class)
    public void testCreateContentWithDataDriven(CsvReader.ContentTestData testData) {
        AuthService.postLogin();

        String latestTrainingId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file.");
        }

        String latestChapterId = JsonHelper.getLatestIdByParentId(FilePaths.CHAPTER_DATA_JSON, "programId", latestTrainingId);
        if (latestChapterId == null) {
            Assert.fail("No chapter ID found for training " + latestTrainingId);
        }

        String actualChapterId = "{latestChapterId}".equals(testData.chapterId) ? latestChapterId : testData.chapterId;

        Map<String, Object> variables = CreateContentVariable.variables(
            testData.title,
            testData.description,
            testData.order,
            testData.article,
            testData.articleType,
            actualChapterId,
            testData.duration,
            testData.isRandomQuestion,
            testData.mediaId,
            testData.thumbnailUrl,
            testData.type
        );

        String query = GraphQlFileReader.readMutation("CreateContent.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                CreateContentResponse responseBody = response.as(CreateContentResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.createContent, "createContent object is null");
                    
                    String contentId = responseBody.data.createContent.id;
                    if (!testData.title.isEmpty()) {
                        JsonHelper.saveIdToJson(FilePaths.CONTENT_DATA_JSON, contentId, testData.title, "chapterId", actualChapterId);
                    }
                    System.out.println("✓ Content created: " + contentId);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.createContent == null,
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
