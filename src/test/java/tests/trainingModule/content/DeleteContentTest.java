package tests.trainingModule.content;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.CsvReader.DeleteContentTestData;
import utils.TestDataProvider;
import utils.GraphQlFileReader;
import utils.JsonHelper;
import utils.FilePaths;
import utils.ExtentManager;
import models.responses.CommonDeleteResponse;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

public class DeleteContentTest extends BaseTest {

    @Test(dataProvider = "deleteContentTestData", dataProviderClass = TestDataProvider.class)
    public void testDeleteContentWithDataDriven(DeleteContentTestData testData) {
        AuthService.postLogin();
        
        String contentId = resolveContentId(testData.id);
        if (contentId == null || contentId.isEmpty()) {
            System.out.println("Skipping: ID not resolved for " + testData.id);
            return;
        }

        Map<String, Object> variables = Map.of("id", contentId);
        String query = GraphQlFileReader.readMutationTraining("DeleteContent.graphql");
        Response response = GraphQlClient.execute(query, variables);
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Content ID: " + contentId);
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.asString());

        if (response.statusCode() == 200) {
            try {
                CommonDeleteResponse deleteResponse = response.as(CommonDeleteResponse.class);
                boolean hasErrors = deleteResponse.errors != null && !deleteResponse.errors.isEmpty();
                boolean isDeleted = deleteResponse.data != null && Boolean.TRUE.equals(deleteResponse.data.deleteContent);

                if ("SUCCESS".equalsIgnoreCase(testData.expectedResult)) {
                    Assert.assertFalse(hasErrors, "Response has errors: " + testData.scenario);
                    Assert.assertTrue(isDeleted, "Content not deleted: " + testData.scenario);
                    System.out.println("✓ Content deleted");
                } else {
                    Assert.assertTrue(hasErrors || !isDeleted, "Deletion should have failed: " + testData.scenario);
                    System.out.println("✓ Expected failure confirmed");
                }
            } catch (Exception e) {
                if (!"FAIL".equalsIgnoreCase(testData.expectedResult)) throw e;
            }
        } else {
            if (!"FAIL".equalsIgnoreCase(testData.expectedResult)) {
                Assert.fail("Request failed with status " + response.statusCode());
            }
        }
    }

    private String resolveContentId(String placeholder) {
        if ("{videoContentId}".equals(placeholder)) {
            return JsonHelper.getIdByFieldValue(FilePaths.CONTENT_DATA_JSON, "name", "Introduction Video");
        } else if ("{articleContentId}".equals(placeholder)) {
            return JsonHelper.getIdByFieldValue(FilePaths.CONTENT_DATA_JSON, "name", "article  SDLC");
        } else if ("{testContentId}".equals(placeholder)) {
            return JsonHelper.getIdByFieldValue(FilePaths.CONTENT_DATA_JSON, "name", "Test ujian sdlc");
        }
        return placeholder;
    }
}
