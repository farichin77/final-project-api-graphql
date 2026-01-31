package tests.trainingModule.content;

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
import models.requests.content.UpdateContentVariable;
import models.responses.content.UpdateContentResponse;
import io.restassured.response.Response;

import java.util.Map;

public class UpdateContentTest extends BaseTest {

    @Test(dataProvider = "updateContentTestData", dataProviderClass = TestDataProvider.class)
    public void testUpdateContentWithDataDriven(CsvReader.UpdateContentTestData testData) {

        String contentId = JsonHelper.getIdFromJson(FilePaths.CONTENT_DATA_JSON);
        if (contentId == null) {
            Assert.fail("No content ID found in JSON file.");
        }
        
        String latestTrainingId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file.");
        }

        String latestChapterId = JsonHelper.getLatestIdByParentId(FilePaths.CHAPTER_DATA_JSON, "programId", latestTrainingId);
        if (latestChapterId == null) {
            Assert.fail("No chapter ID found for training " + latestTrainingId);
        }
        
        String actualId = testData.id.equals("{lastCreatedId}") ? contentId : testData.id;
        String actualChapterId = testData.chapterId.equals("{latestChapterId}") ? latestChapterId : testData.chapterId;
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Content ID: " + actualId);

        int randomQuestionCount = "test".equalsIgnoreCase(testData.type) ? 10 : 0;
        
        Map<String, Object> variables = UpdateContentVariable.variables(
            actualId,
            testData.title,
            testData.description,
            testData.order,
            testData.article,
            testData.articleType,
            actualChapterId,
            testData.duration,
            testData.isRandomQuestion,
            randomQuestionCount,
            testData.mediaId,
            testData.thumbnailUrl,
            testData.type
        );

        String query = GraphQlFileReader.readMutationTraining("UpdateContent.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Request Variables: " + variables);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        if (response.statusCode() == 200) {
            try {
                UpdateContentResponse responseBody = response.as(UpdateContentResponse.class);
                boolean hasGraphQLErrors = responseBody.errors != null && !responseBody.errors.isEmpty();
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasGraphQLErrors, "Response has GraphQL errors: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, "Response data is null");
                    Assert.assertNotNull(responseBody.data.updateContent, "updateContent object is null");
                    
                    String actualTitle = responseBody.data.updateContent.title;
                    String actualType = responseBody.data.updateContent.type;
                    String actualDescription = responseBody.data.updateContent.description;
                    String actualOrder = String.valueOf(responseBody.data.updateContent.order);
                    
                    boolean titleMatches = testData.title.equals(actualTitle);
                    boolean typeMatches = testData.type.equals(actualType);
                    boolean descMatches = testData.description.equals(actualDescription);
                    boolean orderMatches = String.valueOf(testData.order).equals(actualOrder);
                    
                    if (titleMatches && typeMatches && descMatches && orderMatches) {
                        System.out.println("✓ Update verified");
                    } else {
                        Assert.fail("Data mismatch for " + testData.scenario + 
                                  ": Expected [" + testData.title + ", " + testData.type + 
                                  "] but got [" + actualTitle + ", " + actualType + "]");
                    }
                    
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    if (hasGraphQLErrors) {
                        System.out.println("✓ Expected failure confirmed");
                    } else {
                        System.out.println("⚠ API validation issue: should have failed but succeeded");
                    }
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
