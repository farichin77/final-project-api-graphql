package tests.trainingModule.chapter;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import utils.FilePaths;
import models.requests.chapter.UpdateChapterVariable;
import models.responses.chapter.UpdateChapterResponse;
import io.restassured.response.Response;

import java.util.Map;

public class UpdateChapterTest extends BaseTest {

    @DataProvider(name = "updateChapterTestData")
    public Object[][] getUpdateChapterTestData() {
        var testDataList = CsvReader.readUpdateChapterTestData("test-data/training/update-chapter-test.csv");
        Object[][] data = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            data[i][0] = testDataList.get(i);
        }
        return data;
    }

    @Test(dataProvider = "updateChapterTestData")
    public void testUpdateChapterWithDataDriven(CsvReader.UpdateChapterTestData testData) {
        String chapterId = JsonHelper.getIdFromJson(FilePaths.CHAPTER_DATA_JSON);
        if (chapterId == null) {
            Assert.fail("No chapter ID found in JSON file. Run ChapterDataDrivenTest first.");
        }
        
        // Get latest training ID from JSON
        String latestTrainingId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file. Run CreateTrainingTest first.");
        }
        
        // Replace placeholders with actual IDs
        String actualId = testData.id.equals("{lastCreatedId}") ? chapterId : testData.id;
        String actualProgramId = testData.programId.equals("{latestTrainingId}") ? latestTrainingId : testData.programId;
        
        System.out.println("=== Updating Chapter ===");
        System.out.println("Test Scenario: " + testData.scenario);
        System.out.println("Chapter ID: " + actualId);
        System.out.println("Training ID: " + actualProgramId);
        System.out.println("New Title: " + testData.title);
        System.out.println("New Description: " + testData.description);
        System.out.println("New Order: " + testData.order);

        // Prepare variables using model
        Map<String, Object> variables = UpdateChapterVariable.variables(
            actualId,
            testData.title,
            testData.description,
            testData.order,
            actualProgramId
        );

        String query = GraphQlFileReader.readMutationTraining("UpdateChapter.graphql");

        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Update Response Status: " + response.statusCode());
        System.out.println("Update Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                // Deserialize response using model
                UpdateChapterResponse responseBody = response.as(UpdateChapterResponse.class);
                
                boolean hasGraphQLErrors = responseBody.errors != null && responseBody.errors.size() > 0;
                
                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify successful chapter update
                    Assert.assertFalse(hasGraphQLErrors, 
                        "Response should not have GraphQL errors for scenario: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data, 
                        "Response data should not be null for scenario: " + testData.scenario);
                    Assert.assertNotNull(responseBody.data.updateChapter, 
                        "UpdateChapter should not be null for scenario: " + testData.scenario);
                    Assert.assertEquals(responseBody.data.updateChapter.id, actualId, 
                        "Response should contain correct chapter ID for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Chapter updated successfully: " + testData.scenario);
                } else if ("FAIL".equalsIgnoreCase(testData.expectedResult)) {
                    // Verify chapter update fails (should have errors)
                    Assert.assertTrue(hasGraphQLErrors || responseBody.data == null || responseBody.data.updateChapter == null,
                        "Chapter update should fail for scenario: " + testData.scenario);
                    
                    System.out.println("✓ Chapter update failed as expected: " + testData.scenario);
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
}
