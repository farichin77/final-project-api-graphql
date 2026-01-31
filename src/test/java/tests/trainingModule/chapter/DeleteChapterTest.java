package tests.trainingModule.chapter;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import services.AuthService;
import client.GraphQlClient;
import utils.*;
import models.responses.CommonDeleteResponse;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

public class DeleteChapterTest extends BaseTest {

    @Test(dataProvider = "deleteChapterTestData", dataProviderClass = TestDataProvider.class)
    public void testDeleteChapterWithDataDriven(CsvReader.DeleteChapterTestData testData) {
        String latestTrainingId = JsonHelper.getLatestIdFromJson(FilePaths.TRAINING_DATA_JSON);
        if (latestTrainingId == null) {
            Assert.fail("No training ID found in JSON file.");
        }
        
        List<String> chapterIds = JsonHelper.getIdsByParentId(FilePaths.CHAPTER_DATA_JSON, "programId", latestTrainingId);
        if (chapterIds.isEmpty()) {
            Assert.fail("No chapters found for training ID: " + latestTrainingId);
        }
        
        System.out.println("Scenario: " + testData.scenario);
        System.out.println("Training ID: " + latestTrainingId);
        
        int deletedCount = 0;
        for (String chapterId : chapterIds) {
            Map<String, Object> variables = Map.of("id", chapterId);
            String deletePayload = GraphQlFileReader.readMutationTraining("DeleteChapter.graphql");
            Response response = GraphQlClient.execute(deletePayload, variables);

            System.out.println("Deleting Chapter ID: " + chapterId + " | Status: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                try {
                    CommonDeleteResponse deleteResponse = response.as(CommonDeleteResponse.class);
                    if (deleteResponse.data != null && Boolean.TRUE.equals(deleteResponse.data.deleteChapter)) {
                        deletedCount++;
                        System.out.println("✓ Deleted");
                    }
                } catch (Exception e) {}
            }
        }
        
        System.out.println("✓ Chapters deleted: " + deletedCount + "/" + chapterIds.size());
        Assert.assertTrue(deletedCount > 0, "No chapters were deleted");
    }
}
